import com.mylog.dto.LoginResponse;
import com.mylog.dto.social.OAuthRequest;
import com.mylog.interfaces.OAuth2UserInfo;

public interface OAuthService {
    LoginResponse login(OAuthRequest request);
    String getAccessToken(OAuthRequest request);
    OAuth2UserInfo getUserInfo(String accessToken);
}