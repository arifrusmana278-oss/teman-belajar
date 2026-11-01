package id.belajarsd.app;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="questions")
public class Question {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="lesson_id") private Lesson lesson;
 @Column(nullable=false,columnDefinition="text") private String prompt;
 @Column(nullable=false,columnDefinition="text") private String optionsPacked;
 @Column(nullable=false) private Integer correctOptionIndex;
 @Column(nullable=false,columnDefinition="text") private String explanation;
 @Column(length=16) private String source;
 @Column private Instant createdAt;
 @Column private Instant expiresAt;
 protected Question() {}
 public Question(Lesson lesson,String prompt,String optionsPacked,Integer correctOptionIndex,String explanation){this(lesson,prompt,optionsPacked,correctOptionIndex,explanation,"SEED",null);}
 public Question(Lesson lesson,String prompt,String optionsPacked,Integer correctOptionIndex,String explanation,String source,Instant expiresAt){this.lesson=lesson;this.prompt=prompt;this.optionsPacked=optionsPacked;this.correctOptionIndex=correctOptionIndex;this.explanation=explanation;this.source=source;this.expiresAt=expiresAt;}
 @PrePersist void created(){if(createdAt==null)createdAt=Instant.now();}
 public Long getId(){return id;} public Lesson getLesson(){return lesson;} public String getPrompt(){return prompt;} public String[] getOptions(){return optionsPacked.split("\\|\\|\\|",-1);} public Integer getCorrectOptionIndex(){return correctOptionIndex;} public String getExplanation(){return explanation;} public String getSource(){return source==null?"SEED":source;} public Instant getCreatedAt(){return createdAt;} public Instant getExpiresAt(){return expiresAt;}
}
