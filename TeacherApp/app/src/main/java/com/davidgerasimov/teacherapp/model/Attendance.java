package com.davidgerasimov.teacherapp.model;

public class Attendance {
    private int id;
    private int studentId;
    private int subjectId;
    private int teacherId;
    private String timestamp;
    private String status;
    private String studentName;
    private String subjectName;

    public Attendance() {}

    public Attendance(int studentId, int subjectId, int teacherId, String status) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.status = status;
    }

    public int getId() { return id; }
    public int getStudentId() { return studentId; }
    public int getSubjectId() { return subjectId; }
    public int getTeacherId() { return teacherId; }
    public String getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getStudentName() { return studentName; }
    public String getSubjectName() { return subjectName; }

    public void setId(int id) { this.id = id; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setStatus(String status) { this.status = status; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
}