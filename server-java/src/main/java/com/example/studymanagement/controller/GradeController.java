package com.example.studymanagement.controller;

import com.example.studymanagement.model.Grade;
import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
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
    public ResponseEntity<?> update(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody Grade req) {
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
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
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
        List<Grade> toSave = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().skip(1).filter(line -> !line.isBlank()).forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    Grade grade = new Grade();
                    grade.setUserId(user.getId());
                    grade.setCourseCode(parts[0].trim());
                    grade.setCourseName(parts[1].trim());
                    grade.setCredit(parseDouble(parts[2]));
                    grade.setScore(parseDouble(parts[3]));
                    grade.setGradePoint(parts.length > 4 ? parseDouble(parts[4]) : null);
                    grade.setSemester(parts.length > 5 ? parts[5].trim() : null);
                    grade.setAcademicYear(parts.length > 6 ? parts[6].trim() : null);
                    grade.setStatus(grade.getScore() != null && grade.getScore() >= 60 ? "通过" : "未通过");
                    grade.setCreatedAt(LocalDateTime.now());
                    grade.setUpdatedAt(LocalDateTime.now());
                    toSave.add(grade);
                }
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "导入失败", "data", null));
        }
        gradeRepository.saveAll(toSave);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("imported", toSave.size())));
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
