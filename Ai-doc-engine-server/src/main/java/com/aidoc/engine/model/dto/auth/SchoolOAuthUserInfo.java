package com.aidoc.engine.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User identity returned by the school OAuth2 platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolOAuthUserInfo {

    private String userNo;

    private String userName;

    private String email;
}
