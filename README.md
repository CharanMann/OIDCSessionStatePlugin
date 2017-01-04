# OIDCSessionStatePlugin

* Custom OpenAM Scope validator <br />
* This plugin sets OpenAM session state in Token response "session_state" for OIDC Authorization Code and Implicit Grants flows.
* For Resource Owner Password Credentials and Client Credentials Grant flows: this plugin sets "session_state" as DESTROYED as there is no OpenAM session for these flows.   
* There are 4 types of OpenAM session states: INVALID, VALID, INACTIVE, DESTROYED
* Refer OpenID Connect Session Management spec: http://openid.net/specs/openid-connect-session-1_0.html#CreatingUpdatingSessions 
    
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
* Authorization Code Grant:
```
curl -v --request POST --data "grant_type=authorization_code&redirect_uri=http://openam135.example.com:9090/openid/redirect.html&code=c2545771-2efd-4a22-8a59-c06cbe161fc5" --user "myClientID:password" --cookie "iplanetDirectoryPro=AQIC5wM2LY4SfcxBpYeD4tx2AGGe7_gpQ21hjdNY1MENxTc.*AAJTSQACMDEAAlNLABM0Mjc2MDgwNTQ2ODA2MjYzNzI2AAJTMQAA*" http://openam135.example.com:9090/openam/oauth2/employees/access_token
  Trying 192.168.56.108...
  Connected to openam135.example.com (192.168.56.108) port 9090 (#0)
  Server auth using Basic with user 'myClientID'
> POST /openam/oauth2/employees/access_token HTTP/1.1
> Host: openam135.example.com:9090
> Authorization: Basic bXlDbGllbnRJRDpwYXNzd29yZA==
> User-Agent: curl/7.43.0
> Accept: */*
> Cookie: iplanetDirectoryPro=AQIC5wM2LY4SfcxBpYeD4tx2AGGe7_gpQ21hjdNY1MENxTc.*AAJTSQACMDEAAlNLABM0Mjc2MDgwNTQ2ODA2MjYzNzI2AAJTMQAA*
> Content-Length: 139
> Content-Type: application/x-www-form-urlencoded
>
  upload completely sent off: 139 out of 139 bytes
< HTTP/1.1 200 OK
< Cache-Control: no-store
< Date: Wed, 04 Jan 2017 19:23:49 GMT
< Accept-Ranges: bytes
< Server: Restlet-Framework/2.3.4
< Vary: Accept-Charset, Accept-Encoding, Accept-Language, Accept
< Pragma: no-cache
< Content-Type: application/json
< Transfer-Encoding: chunked
<
  Connection #0 to host openam135.example.com left intact
{"session_state":"VALID","scope":"mail openid profile","expires_in":3579,"token_type":"Bearer","refresh_token":"4c87a0b2-fee2-4e1f-b365-da274ca5312c","id_token":"eyAidHlwIjogIkpXVCIsICJhbGciOiAiSFMyNTYiIH0.eyAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF6cCI6ICJteUNsaWVudElEIiwgInN1YiI6ICJjaGFyYW4iLCAiYXRfaGFzaCI6ICJnc1JOUGdWVW1oTmhpbHBYYjJORWJnIiwgImlzcyI6ICJodHRwOi8vb3BlbmFtMTM1LmV4YW1wbGUuY29tOjkwOTAvb3BlbmFtL29hdXRoMi9lbXBsb3llZXMiLCAib3JnLmZvcmdlcm9jay5vcGVuaWRjb25uZWN0Lm9wcyI6ICIyZmIwNTdkNy01Y2QwLTRlMDctYjA1Yi01NGUyMmUwN2I1MzAiLCAiaWF0IjogMTQ4MzU1NzgwOSwgImF1dGhfdGltZSI6IDE0ODM1NTc0NzIsICJleHAiOiAxNDgzNTYxNDA5LCAidG9rZW5UeXBlIjogIkpXVFRva2VuIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI5NjkzNTExNy02YjRhLTRmMmMtOTQyNC0yMWVlNTM3YzA4ZGQtMTE1IiwgInJlYWxtIjogIi9lbXBsb3llZXMiLCAiYXVkIjogIm15Q2xpZW50SUQiLCAiY19oYXNoIjogIkE2N0tfeFhFSWtyN2JsaEZMdV9RckEiIH0.CrnnGul4PkoiAPPHWbC1nK85hwD4SqQDgGBBf4qblT0","access_token":"ede4d90f-a396-4b4a-81cf-d30154e538bb"}
```

* Implicit Grant: 
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

* Resource Owner Password Credentials Grant:
```
curl -X POST -H "Authorization: BASIC bXlDbGllbnRJRDpwYXNzd29yZA==" -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=password&username=charan&password=password&scope=openid profile mail' "http://openam135.example.com:9090/openam/oauth2/employees/access_token"

{"session_state":"DESTROYED","scope":"mail openid profile","expires_in":3594,"token_type":"Bearer","refresh_token":"c1688794-4921-445d-aa2d-dfc03b67f51e","id_token":"eyAidHlwIjogIkpXVCIsICJhbGciOiAiSFMyNTYiIH0.eyAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF6cCI6ICJteUNsaWVudElEIiwgInN1YiI6ICJjaGFyYW4iLCAiYXRfaGFzaCI6ICIyX19ONy1CMnBGeElRT2N4ek1Sd3pBIiwgImlzcyI6ICJodHRwOi8vb3BlbmFtMTM1LmV4YW1wbGUuY29tOjkwOTAvb3BlbmFtL29hdXRoMi9lbXBsb3llZXMiLCAib3JnLmZvcmdlcm9jay5vcGVuaWRjb25uZWN0Lm9wcyI6ICI2MTEyNjUyMC03ZDc0LTQxOTgtYWUyNi0yMTE3YmM0MWNlM2QiLCAiaWF0IjogMTQ4MzU1ODY0MywgImF1dGhfdGltZSI6IDE0ODM1NTg2NDMsICJleHAiOiAxNDgzNTYyMjQzLCAidG9rZW5UeXBlIjogIkpXVFRva2VuIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI5NjkzNTExNy02YjRhLTRmMmMtOTQyNC0yMWVlNTM3YzA4ZGQtMTQzIiwgInJlYWxtIjogIi9lbXBsb3llZXMiLCAiYXVkIjogIm15Q2xpZW50SUQiIH0.4eM_AqGwdzNdeCVZX4CGJDv2aEw9l9XN-w3Rjrp548c","access_token":"43514c7b-7e42-491c-bca7-4d79996031ee"}
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
