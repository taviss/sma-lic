package models;

import lombok.Data;
import play.data.validation.Constraints;

import javax.persistence.*;

@Data
@Entity
@Table(name = "images")
public class Image {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "i_class", nullable = false)
    @Constraints.Required
    private String imageClass;
    
    @Column(name = "i_path", nullable = false)
    private String imagePath;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;
    
    @Column(name = "i_last_seen")
    private String lastSeenImage;
}
