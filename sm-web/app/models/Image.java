package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private User owner;
    
    @Column(name = "i_last_seen")
    private String lastSeenImage;
    
    @Column(name = "i_trainable")
    private Boolean trainable;

    @Column(name = "i_left")
    private Float boxLeft;

    @Column(name = "i_top")
    private Float boxTop;

    @Column(name = "i_right")
    private Float boxRight;

    @Column(name = "i_bottom")
    private Float boxBottom;

}
