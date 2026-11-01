package id.belajarsd.app;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="learning_progress")
public class Progress {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=80) private String studentName;
 @ManyToOne(optional=false) @JoinColumn(name="lesson_id") private Lesson lesson;
 @Column(nullable=false) private Integer score;
 @Column(nullable=false) private Integer total;
 @Column(nullable=false) private Instant completedAt;
 protected Progress() {}
 public Progress(String studentName,Lesson lesson,Integer score,Integer total){this.studentName=studentName;this.lesson=lesson;this.score=score;this.total=total;this.completedAt=Instant.now();}
 public Long getId(){return id;} public String getStudentName(){return studentName;} public Lesson getLesson(){return lesson;} public Integer getScore(){return score;} public Integer getTotal(){return total;} public Instant getCompletedAt(){return completedAt;}
}
