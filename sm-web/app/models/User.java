package models;

import lombok.Data;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.util.List;

/**
 * Created by octavian.salcianu on 8/29/2016.
 */

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "u_name", nullable = false)
    @Constraints.MinLength(3)
    @Constraints.MaxLength(64)
    @Constraints.Required
    private String userName;

    @Column(name = "u_mail", nullable = false)
    @Constraints.Required
    @Constraints.Email
    @Constraints.MaxLength(45)
    private String userMail;

    @Column(name = "u_token", nullable = false)
    private String userToken;

    @Column(name = "u_active", nullable = false)
    private Boolean userActive;

    @Column(name = "u_pass", nullable = false)
    @Constraints.MinLength(6)
    @Constraints.MaxLength(256)
    @Constraints.Required
    private String userPass;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "owner")
    private List<CameraAddress> cameraAddresses;
}
