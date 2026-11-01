package id.belajarsd.app;
import jakarta.persistence.*;
@Entity @Table(name="subjects")
public class Subject {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, unique=true) private String name;
 @Column(nullable=false) private String icon;
 @Column(nullable=false, length=300) private String description;
 @Column(nullable=false) private String color;
 @Column(nullable=false) private Integer sortOrder;
 protected Subject() {}
 public Subject(String name,String icon,String description,String color,Integer sortOrder){this.name=name;this.icon=icon;this.description=description;this.color=color;this.sortOrder=sortOrder;}
 public Long getId(){return id;} public String getName(){return name;} public String getIcon(){return icon;} public String getDescription(){return description;} public String getColor(){return color;} public Integer getSortOrder(){return sortOrder;}
}
