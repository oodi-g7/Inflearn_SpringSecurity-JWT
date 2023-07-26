package test.security.config.oauth.provider;

public interface OAuth2UserInfo {
	
	String getProviderId(); // google의 pk, facebook의 pk, ...
	String getProvider(); // google, facebook, ...
	String getEmail();
	String getName();

}
