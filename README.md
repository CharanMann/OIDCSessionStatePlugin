# OIDCSessionStatePlugin

* Custom OpenAM Scope validator <br />
* This plugin sets OpenAM session state in Token response "session_state"  for OAuth Authorization Code and Implicit flows <br />
* Refer OAuth spec: http://openid.net/specs/openid-connect-session-1_0.html#CreatingUpdatingSessions 


Pre-requisites :
================
1. OpenAM has been installed and configured.
2. OpenAM has been configured as OIDC provider. Check https://backstage.forgerock.com/docs/openam/13.5/admin-guide#configure-openid-connect-provider 
3. Maven has been installed and configured.

OpenAM Configuration:
=====================
1. Build OIDCSessionStatePlugin by running 'mvn clean install'. This will build openam-oidc-session-state-1.0.0-SNAPSHOT.jar under /target directory.
2. Stop OpenAM. 
3. Copy openam-oidc-session-state-1.0.0-SNAPSHOT.jar to (OpenAM-TomcatHome)/webapps/ROOT/WEB-INF/lib
4. Restart OpenAM
5. Specify "org.forgerock.openam.examples.OIDCSessionStateValidator" under Realms> (Specific realm)> Services> OAuth2 Provider> Scope Implementation Class
  
Testing:
======== 
* Implicit flow: 
```
curl -v --request POST --data "response_type=id_token%20token&scope=mail%20openid%20profile&client_id=myClientID&save_consent=1&decision=Allow&redirect_uri=http://openam135.example.com:9090/openid/redirect.html&nonce=1234&csrf=AQIC5wM2LY4SfczjdeBGEePxYOpOFq26BqFlT-9um_EvlcQ.*AAJTSQACMDEAAlNLABQtMTAyMjMyNDM0NTU5ODQ4Mjg0MgACUzEAAA..*" --cookie "iplanetDirectoryPro=AQIC5wM2LY4SfczjdeBGEePxYOpOFq26BqFlT-9um_EvlcQ.*AAJTSQACMDEAAlNLABQtMTAyMjMyNDM0NTU5ODQ4Mjg0MgACUzEAAA..*" http://openam135.example.com:9090/openam/oauth2/employees/authorize
  Trying 192.168.56.108...
  Connected to openam135.example.com (192.168.56.108) port 9090 (#0)
> POST /openam/oauth2/employees/authorize HTTP/1.1
> Host: openam135.example.com:9090
> User-Agent: curl/7.43.0
> Accept: */*
> Cookie: iplanetDirectoryPro=AQIC5wM2LY4SfczjdeBGEePxYOpOFq26BqFlT-9um_EvlcQ.*AAJTSQACMDEAAlNLABQtMTAyMjMyNDM0NTU5ODQ4Mjg0MgACUzEAAA..*
> Content-Length: 302
> Content-Type: application/x-www-form-urlencoded
>
 
  upload completely sent off: 302 out of 302 bytes
< HTTP/1.1 302 Found
< Cache-Control: no-store
< Date: Wed, 04 Jan 2017 18:35:30 GMT
< Accept-Ranges: bytes
< Location: http://openam135.example.com:9090/openid/redirect.html#session_state=VALID&scope=mail%20openid%20profile&token_type=Bearer&expires_in=3599&id_token=eyAidHlwIjogIkpXVCIsICJhbGciOiAiSFMyNTYiIH0.eyAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF6cCI6ICJteUNsaWVudElEIiwgInN1YiI6ICJjaGFyYW4iLCAiYXRfaGFzaCI6ICJld1BueWVqTTVVQ3dkSzJjUzZaNXdRIiwgImlzcyI6ICJodHRwOi8vb3BlbmFtMTM1LmV4YW1wbGUuY29tOjkwOTAvb3BlbmFtL29hdXRoMi9lbXBsb3llZXMiLCAib3JnLmZvcmdlcm9jay5vcGVuaWRjb25uZWN0Lm9wcyI6ICI5OTI4YzkzYi00ZGRmLTRiMGItYmZkMy0zYTVjODgxY2I4NWQiLCAiaWF0IjogMTQ4MzU1NDkzMCwgImF1dGhfdGltZSI6IDE0ODM1NTQ4OTQsICJleHAiOiAxNDgzNTU4NTMwLCAidG9rZW5UeXBlIjogIkpXVFRva2VuIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJiYTRmODU5OC00ZWExLTQ5NmYtYmY3NS01N2I5N2IzMmMwOTUtNjciLCAibm9uY2UiOiAiMTIzNCIsICJyZWFsbSI6ICIvZW1wbG95ZWVzIiwgImF1ZCI6ICJteUNsaWVudElEIiB9.03izYMYGjARQcfFcEH696WWyQPL0aGmEjVTDWoy_Ojc&access_token=c5004e95-3993-49e5-9683-95b620a4c9cf
< Server: Restlet-Framework/2.3.4
< Vary: Accept-Charset, Accept-Encoding, Accept-Language, Accept
< Pragma: no-cache
< Content-Length: 0
<
Connection #0 to host openam135.example.com left intact
```


* * *

Copyright © 2016 ForgeRock, AS.

This is unsupported code made available by ForgeRock for community development subject to the license detailed below. The code is provided on an "as is" basis, without warranty of any kind, to the fullest extent permitted by law. 

ForgeRock does not warrant or guarantee the individual success developers may have in implementing the code on their development platforms or in production configurations.

ForgeRock does not warrant, guarantee or make any representations regarding the use, results of use, accuracy, timeliness or completeness of any data or information relating to the alpha release of unsupported code. ForgeRock disclaims all warranties, expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related to the code, or any service or software related thereto.

ForgeRock shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any action taken by you or others related to the code.

The contents of this file are subject to the terms of the Common Development and Distribution License (the License). You may not use this file except in compliance with the License.

You can obtain a copy of the License at https://forgerock.org/cddlv1-0/. See the License for the specific language governing permission and limitations under the License.

Portions Copyrighted 2016 Charan Mann
