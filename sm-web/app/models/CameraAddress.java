package models;

import lombok.Data;
import play.data.validation.Constraints;

import javax.persistence.*;

@Data
@Entity
@Table(name = "cameras")
public class CameraAddress {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "c_address", nullable = false)
    @Constraints.MinLength(3)
    @Constraints.MaxLength(64)
    @Constraints.Required
    private String address;

    @Column(name = "c_user", nullable = false)
    @Constraints.MinLength(3)
    @Constraints.MaxLength(64)
    @Constraints.Required
    private String user;

    @Column(name = "c_password", nullable = false)
    @Constraints.MinLength(6)
    @Constraints.MaxLength(256)
    @Constraints.Required
    private String password;
    
    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;
}
