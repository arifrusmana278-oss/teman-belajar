package id.belajarsd.app;
import jakarta.persistence.*;
@Entity @Table(name="lessons")
public class Lesson {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="subject_id") private Subject subject;
 @Column(nullable=false) private String title;
 @Column(nullable=false) private String grade;
 @Column(nullable=false,columnDefinition="text") private String content;
 @Column(nullable=false) private Integer estimatedMinutes;
 protected Lesson() {}
 public Lesson(Subject subject,String title,String grade,String content,Integer estimatedMinutes){this.subject=subject;this.title=title;this.grade=grade;this.content=content;this.estimatedMinutes=estimatedMinutes;}
 public Long getId(){return id;} public Subject getSubject(){return subject;} public String getTitle(){return title;} public String getGrade(){return grade;} public String getContent(){return content;} public Integer getEstimatedMinutes(){return estimatedMinutes;}
 public void updateMaterial(String content,Integer estimatedMinutes){this.content=content;this.estimatedMinutes=estimatedMinutes;}
 public void renameMaterial(String title,String content,Integer estimatedMinutes){this.title=title;updateMaterial(content,estimatedMinutes);}
}
