package onedu.blue.member.exceptions;

import onedu.blue.global.exceptions.CommonException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends CommonException {

    public MemberNotFoundException() {

        super("NofFound.member", HttpStatus.NOT_FOUND);

        setErrorCode(true);
    }
}