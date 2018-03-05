package forms;

import play.data.validation.Constraints;

/**
 * Created by octavian.salcianu on 8/29/2016.
 */
public class LoginForm {

    @Constraints.Required()
    @Constraints.MinLength(3)
    @Constraints.MaxLength(64)
    public String userName;

    @Constraints.Required()
    @Constraints.MinLength(6)
    @Constraints.MaxLength(256)
    public String userPass;
}
