package org.wso2.carbon.user.core.dto;

public class RoleV2DTO {

    private String name;
    private String audience;
    private String audienceId;

    public RoleV2DTO(String name, String audience, String audienceId) {

        this.name = name;
        this.audience = audience;
        this.audienceId = audienceId;
    }

    public RoleV2DTO(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getAudience() {

        return audience;
    }

    public void setAudience(String audience) {

        this.audience = audience;
    }

    public String getAudienceId() {

        return audienceId;
    }

    public void setAudienceId(String audienceId) {

        this.audienceId = audienceId;
    }
}
