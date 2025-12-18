package com.example.studymanagement.controller;

import com.example.studymanagement.model.Grade;
import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeRepository gradeRepository;

    @GetMapping
    public ResponseEntity<?> list(
        @AuthenticationPrincipal User user,
        @RequestParam(name = "semester", required = false) String semester,
        @RequestParam(name = "academicYear", required = false) String academicYear,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "courseType", required = false) String courseType,
        @RequestParam(name = "creditMin", required = false) Double creditMin,
        @RequestParam(name = "creditMax", required = false) Double creditMax,
        @RequestParam(name = "scoreMin", required = false) Double scoreMin,
        @RequestParam(name = "scoreMax", required = false) Double scoreMax,
        @RequestParam(name = "gpaMin", required = false) Double gpaMin,
        @RequestParam(name = "gpaMax", required = false) Double gpaMax,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "ids", required = false) List<Long> ids
    ) {
        List<Grade> list = filterGrades(user.getId(), semester, academicYear, translateStatus(status), courseType,
            creditMin, creditMax, scoreMin, scoreMax, gpaMin, gpaMax, keyword, ids);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("list", list, "total", list.size())));
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal User user, @RequestBody Grade grade) {
        grade.setId(null);
        grade.setUserId(user.getId());
        grade.setStatus(grade.getScore() != null && grade.getScore() >= 60 ? "通过" : "未通过");
        gradeRepository.save(grade);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("id", grade.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal User user, @PathVariable("id") Long id, @RequestBody Grade req) {
        Grade g = gradeRepository.findById(id).orElseThrow();
        if (!g.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限", "data", null));
        }
        req.setId(id);
        req.setUserId(user.getId());
        req.setStatus(req.getScore() != null && req.getScore() >= 60 ? "通过" : "未通过");
        gradeRepository.save(req);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user, @PathVariable("id") Long id) {
        gradeRepository.findById(id)
            .filter(g -> g.getUserId().equals(user.getId()))
            .ifPresent(gradeRepository::delete);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(@AuthenticationPrincipal User user) {
        List<Grade> list = gradeRepository.findAll().stream()
            .filter(g -> g.getUserId().equals(user.getId()))
            .collect(Collectors.toList());

        double totalCredits = list.stream().mapToDouble(g -> g.getCredit() == null ? 0 : g.getCredit()).sum();
        double passedCredits = list.stream()
            .filter(g -> "通过".equals(g.getStatus()) || (g.getScore() != null && g.getScore() >= 60))
            .mapToDouble(g -> g.getCredit() == null ? 0 : g.getCredit())
            .sum();
        long totalCourses = list.size();
        long passedCourses = list.stream()
            .filter(g -> "通过".equals(g.getStatus()) || (g.getScore() != null && g.getScore() >= 60))
            .count();

        double totalGPA = calculateGPA(list);

        String currentSemester = list.stream()
            .map(Grade::getSemester)
            .filter(Objects::nonNull)
            .filter(s -> !s.isEmpty())
            .sorted(Comparator.reverseOrder())
            .findFirst()
            .orElse(null);
        double currentSemesterGPA = calculateGPA(
            list.stream().filter(g -> g.getSemester() != null && g.getSemester().equals(currentSemester)).collect(Collectors.toList())
        );

        long passRate = totalCourses == 0 ? 0 : Math.round((passedCourses * 100.0) / totalCourses);

        return ResponseEntity.ok(Map.of(
            "code", 0,
            "message", "ok",
            "data", Map.of(
                "currentSemesterGPA", currentSemesterGPA,
                "totalGPA", totalGPA,
                "passRate", passRate,
                "totalCredits", totalCredits,
                "passedCredits", passedCredits,
                "totalCourses", totalCourses,
                "passedCourses", passedCourses,
                "trend", buildTrend(list)
            )
        ));
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> export(
        @AuthenticationPrincipal User user,
        @RequestParam(name = "type", defaultValue = "all") String type,
        @RequestParam(name = "ids", required = false) List<Long> ids
    ) {
        List<Grade> list = "selected".equalsIgnoreCase(type) && ids != null
            ? filterGrades(user.getId(), null, null, null, null, null, null, null, null, null, null, null, ids)
            : filterGrades(user.getId(), null, null, null, null, null, null, null, null, null, null, null, null);

        StringBuilder builder = new StringBuilder();
        builder.append("course_code,course_name,credit,score,grade_point,semester,academic_year,status\n");
        list.forEach(g -> builder.append(String.join(",",
            quote(g.getCourseCode()),
            quote(g.getCourseName()),
            String.valueOf(g.getCredit() == null ? "" : g.getCredit()),
            String.valueOf(g.getScore() == null ? "" : g.getScore()),
            String.valueOf(g.getGradePoint() == null ? "" : g.getGradePoint()),
            quote(g.getSemester()),
            quote(g.getAcademicYear()),
            quote(g.getStatus())
        )).append("\n"));

        byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grades.csv")
            .contentLength(bytes.length)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importGrades(@AuthenticationPrincipal User user, @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "文件不能为空", "data", null));
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!List.of("csv", "xlsx", "xls").contains(extension)) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "仅支持 CSV 或 Excel 文件", "data", null));
        }

        List<ImportRow> rows;
        try {
            rows = "xlsx".equals(extension) || "xls".equals(extension)
                ? parseExcelRows(file)
                : parseCsvRows(file);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage(), "data", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "文件无法读取", "data", null));
        }

        if (rows.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "文件中没有可导入的数据", "data", null));
        }

        List<Grade> toSave = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (ImportRow row : rows) {
            try {
                Grade grade = buildGradeFromRow(row.getValues(), user.getId());
                if (grade != null) {
                    toSave.add(grade);
                }
            } catch (IllegalArgumentException e) {
                errors.add("第" + row.getRowNumber() + "行：" + e.getMessage());
            }
        }

        if (toSave.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "没有记录通过校验",
                "data", Map.of("errors", errors)
            ));
        }

        gradeRepository.saveAll(toSave);
        Map<String, Object> result = new HashMap<>();
        result.put("imported", toSave.size());
        result.put("failed", errors.size());
        result.put("errors", errors);

        String message = errors.isEmpty() ? "导入成功" : "部分记录导入成功";
        return ResponseEntity.ok(Map.of("code", 0, "message", message, "data", result));
    }

    private List<ImportRow> parseCsvRows(MultipartFile file) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("文件为空");
            }
            List<String> headers = new ArrayList<>();
            for (String header : splitCsvLine(headerLine)) {
                headers.add(normalizeHeader(header));
            }
            if (headers.stream().noneMatch(StringUtils::hasText)) {
                throw new IllegalArgumentException("未检测到表头");
            }
            String line;
            int rowNumber = 2;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    rowNumber++;
                    continue;
                }
                List<String> values = splitCsvLine(line);
                Map<String, String> valueMap = new HashMap<>();
                for (int i = 0; i < headers.size() && i < values.size(); i++) {
                    String header = headers.get(i);
                    if (StringUtils.hasText(header)) {
                        valueMap.put(header, cleanValue(values.get(i)));
                    }
                }
                if (!valueMap.isEmpty() && !isRowEmpty(valueMap)) {
                    rows.add(new ImportRow(rowNumber, valueMap));
                }
                rowNumber++;
            }
        }
        return rows;
    }

    private List<ImportRow> parseExcelRows(MultipartFile file) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                return rows;
            }
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("Excel 文件缺少表头");
            }
            int headerSize = headerRow.getLastCellNum();
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerSize; i++) {
                headers.add(normalizeHeader(getCellValue(headerRow.getCell(i))));
            }
            if (headers.stream().noneMatch(StringUtils::hasText)) {
                throw new IllegalArgumentException("Excel 文件缺少有效表头");
            }
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Map<String, String> valueMap = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    if (!StringUtils.hasText(header)) {
                        continue;
                    }
                    String value = getCellValue(row.getCell(j));
                    valueMap.put(header, cleanValue(value));
                }
                if (!valueMap.isEmpty() && !isRowEmpty(valueMap)) {
                    rows.add(new ImportRow(i + 1, valueMap));
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Excel 文件解析失败");
        }
        return rows;
    }

    private List<String> splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        if (line == null) {
            return values;
        }
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }

    private String normalizeHeader(String header) {
        if (!StringUtils.hasText(header)) {
            return "";
        }
        String normalized = header
            .replace("\uFEFF", "")
            .replace("（", "(")
            .replace("）", ")")
            .toLowerCase();
        normalized = normalized.replaceAll("\\(.*?\\)", "");
        normalized = normalized.replaceAll("[\\s_\\-]", "");
        switch (normalized) {
            case "课程代码":
            case "coursecode":
            case "courseid":
            case "code":
                return "courseCode";
            case "课程名称":
            case "coursename":
            case "name":
                return "courseName";
            case "学分":
            case "credit":
            case "credits":
                return "credit";
            case "成绩":
            case "score":
            case "grade":
                return "score";
            case "绩点":
            case "gpa":
            case "gradepoint":
                return "gradePoint";
            case "课程类型":
            case "coursetype":
            case "type":
                return "courseType";
            case "学期":
            case "semester":
                return "semester";
            case "学年":
            case "academicyear":
                return "academicYear";
            case "是否必修":
            case "isrequired":
            case "required":
                return "isRequired";
            default:
                return normalized;
        }
    }

    private String cleanValue(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replace("\uFEFF", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private Grade buildGradeFromRow(Map<String, String> row, Long userId) {
        String courseCode = cleanValue(row.get("courseCode"));
        String courseName = cleanValue(row.get("courseName"));
        if (!StringUtils.hasText(courseCode) || !StringUtils.hasText(courseName)) {
            throw new IllegalArgumentException("课程代码与课程名称不能为空");
        }
        Double credit = parseDouble(row.get("credit"));
        if (credit == null || credit <= 0) {
            throw new IllegalArgumentException("学分缺失或格式错误");
        }
        Double score = parseDouble(row.get("score"));
        if (score == null || score < 0 || score > 100) {
            throw new IllegalArgumentException("成绩缺失或不在 0-100 范围内");
        }

        Grade grade = new Grade();
        grade.setUserId(userId);
        grade.setCourseCode(courseCode);
        grade.setCourseName(courseName);
        grade.setCredit(credit);
        grade.setScore(score);
        Double gradePoint = parseDouble(row.get("gradePoint"));
        grade.setGradePoint(gradePoint != null ? gradePoint : scoreToGpa(score));
        grade.setSemester(cleanValue(row.get("semester")));
        grade.setAcademicYear(cleanValue(row.get("academicYear")));
        String courseType = normalizeCourseType(row.get("courseType"));
        grade.setCourseType(courseType);
        grade.setIsRequired(determineIsRequired(row.get("isRequired"), courseType));
        grade.setStatus(score >= 60 ? "通过" : "未通过");
        grade.setCreatedAt(LocalDateTime.now());
        grade.setUpdatedAt(LocalDateTime.now());
        return grade;
    }

    private String normalizeCourseType(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "必修":
            case "required":
            case "core":
                return "required";
            case "选修":
            case "elective":
                return "elective";
            case "通识":
            case "general":
                return "general";
            case "实践":
            case "实验":
            case "practice":
            case "实训":
                return "practice";
            default:
                return value;
        }
    }

    private Boolean determineIsRequired(String raw, String courseType) {
        Boolean parsed = parseBooleanValue(raw);
        if (parsed != null) {
            return parsed;
        }
        if ("required".equalsIgnoreCase(courseType)) {
            return true;
        }
        if ("elective".equalsIgnoreCase(courseType)) {
            return false;
        }
        return null;
    }

    private Boolean parseBooleanValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        if (List.of("是", "yes", "y", "true", "1", "必修").contains(normalized)) {
            return true;
        }
        if (List.of("否", "no", "n", "false", "0", "选修").contains(normalized)) {
            return false;
        }
        return null;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private boolean isRowEmpty(Map<String, String> values) {
        return values.values().stream().allMatch(v -> v == null || v.isBlank());
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "csv";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private static class ImportRow {
        private final int rowNumber;
        private final Map<String, String> values;

        ImportRow(int rowNumber, Map<String, String> values) {
            this.rowNumber = rowNumber;
            this.values = values;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public Map<String, String> getValues() {
            return values;
        }
    }

    private List<Grade> filterGrades(Long userId,
                                     String semester,
                                     String academicYear,
                                     String status,
                                     String courseType,
                                     Double creditMin,
                                     Double creditMax,
                                     Double scoreMin,
                                     Double scoreMax,
                                     Double gpaMin,
                                     Double gpaMax,
                                     String keyword,
                                     List<Long> ids) {
        return gradeRepository.findAll().stream()
            .filter(g -> g.getUserId().equals(userId))
            .filter(g -> semester == null || semester.isBlank() || semester.equals(g.getSemester()))
            .filter(g -> academicYear == null || academicYear.isBlank() || academicYear.equals(g.getAcademicYear()))
            .filter(g -> status == null || status.isBlank() || status.equals(g.getStatus()))
            .filter(g -> courseType == null || courseType.isBlank() || courseType.equals(g.getCourseType()))
            .filter(g -> creditMin == null || g.getCredit() == null || g.getCredit() >= creditMin)
            .filter(g -> creditMax == null || g.getCredit() == null || g.getCredit() <= creditMax)
            .filter(g -> scoreMin == null || g.getScore() == null || g.getScore() >= scoreMin)
            .filter(g -> scoreMax == null || g.getScore() == null || g.getScore() <= scoreMax)
            .filter(g -> gpaMin == null || g.getGradePoint() == null || g.getGradePoint() >= gpaMin)
            .filter(g -> gpaMax == null || g.getGradePoint() == null || g.getGradePoint() <= gpaMax)
            .filter(g -> keyword == null || keyword.isBlank() || matchesKeyword(g, keyword))
            .filter(g -> ids == null || ids.isEmpty() || ids.contains(g.getId()))
            .sorted(Comparator.comparing(Grade::getSemester, Comparator.nullsLast(String::compareTo)).reversed())
            .collect(Collectors.toList());
    }

    private boolean matchesKeyword(Grade grade, String keyword) {
        String key = keyword.toLowerCase();
        return (grade.getCourseName() != null && grade.getCourseName().toLowerCase().contains(key))
            || (grade.getCourseCode() != null && grade.getCourseCode().toLowerCase().contains(key));
    }

    private double calculateGPA(List<Grade> gradeList) {
        double totalPoints = 0;
        double totalCredits = 0;
        for (Grade g : gradeList) {
            double credit = g.getCredit() == null ? 0 : g.getCredit();
            double gp = g.getGradePoint() != null ? g.getGradePoint() : scoreToGpa(g.getScore());
            totalPoints += gp * credit;
            totalCredits += credit;
        }
        if (totalCredits == 0) return 0;
        return totalPoints / totalCredits;
    }

    private double scoreToGpa(Double score) {
        if (score == null) {
            return 0;
        }
        if (score >= 90) return 4.0;
        if (score >= 85) return 3.7;
        if (score >= 82) return 3.3;
        if (score >= 78) return 3.0;
        if (score >= 75) return 2.7;
        if (score >= 72) return 2.3;
        if (score >= 68) return 2.0;
        if (score >= 64) return 1.5;
        if (score >= 60) return 1.0;
        return 0;
    }

    private List<Map<String, Object>> buildTrend(List<Grade> gradeList) {
        return gradeList.stream()
            .filter(g -> g.getSemester() != null && !g.getSemester().isEmpty())
            .collect(Collectors.groupingBy(Grade::getSemester))
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> {
                Map<String, Object> item = new HashMap<>();
                item.put("semester", e.getKey());
                item.put("gpa", calculateGPA(e.getValue()));
                return item;
            })
            .collect(Collectors.toList());
    }

    private String translateStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        if ("passed".equalsIgnoreCase(status)) {
            return "通过";
        }
        if ("failed".equalsIgnoreCase(status)) {
            return "未通过";
        }
        return status;
    }

    private Double parseDouble(String value) {
        try {
            return value == null || value.isBlank() ? null : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String quote(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
