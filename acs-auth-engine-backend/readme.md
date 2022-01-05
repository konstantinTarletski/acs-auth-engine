This project is supposed to serve authentication-related requests for 3DS ACS application.


### Used technologies and tools

* Java 11
* Gradle
* H2 database
* Lombok
* Spring Boot 2.4.0
* JUnit 5

### Artifactory

Add to ".gradle/gradle.properties" file those parameters

```
artifactory.url=https://artifactory.onelum.host/artifactory/
artifactory.username=Artifactory-public-user-olc
artifactory.password=???
```

### Connect to database
  
 You can enable H2 console and use it, here is the configuration to it.

    Link : `http://localhost:8080/h2/`

    JDBC URL: `jdbc:h2:tcp://localhost:9090/mem:testdb`
    
    USER : `acs_auth_engine`
    
    PASSWORD : `secret` 


### Run application

**JAR file is the same for ALL 3 countries, different only configuration files for countries !!!** 

JAR file should be in same folder with those files:

```
translations.json
application-base.properties
application-country.properties
ciam-rsa-private-key.key
keystore.p12
```

If all those files are present (and correct), then application will start without any problems. 

Command to start applications is, (PORT and SPRING-PROFILE (country) already defined inside those "properties" files):

`java -jar acs-auth-engine-backend-0.0.1-SNAPSHOT.jar`

If, for some reason, you want define same another port or country, you can do it from command line:

`java -jar -Dserver.port=9999 -Dspring.profiles.active=lt acs-auth-engine-backend-0.0.1-SNAPSHOT.jar`

(available profiles: `lt`, `lv`, `ee`):

**But I do not see reason to do that way, I recommend modifying property file instead. See Configuration chapter (Level 3)**


Application supports external translation file`translations.json`, but if this file is not present or corrupted, then application will "take" internal translation file.

`ciam-rsa-private-key.key` - Is private Rsa Key for CIAM ( needed for Estonia.)


#### Configuration files

All external configuration files in this folder :

`acs-auth-engine\backend_config`

There is list of files and description of them:

```
application-base-dev.properties
application-base-test.properties
application-country-ee.properties
application-country-lt.properties
application-country-lv.properties
ciam-rsa-private-key.key
keystore.p12
```

Base configurations (level 2) for different ENV (`test` and `dev`)

You just need to put it in correct folder and rename to `application-base.properties`

**Also put correct values if needed**
```
application-base-dev.properties
application-base-test.properties
```

Country specific properties (level 3) for different countries (`ee`, `lt` and `lv`)

You just need to put it in correct folder and rename to `application-country.properties`

**Also put correct values if needed**
```
application-country-ee.properties
application-country-lt.properties
application-country-lv.properties
```

SSL certificate file and CIAM private key (for Estonia)
```
ciam-rsa-private-key.key
keystore.p12
```

## Certificates

#### Self generated Certificate

Article used for generate certificate:

https://dzone.com/articles/how-to-enable-the-https-into-spring-boot-applicati

Generate certificate keytool command:
```
keytool -genkeypair -alias ACS_2_TEST -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650 -storepass password
```
To test the content of a keystore:
```
keytool -list -v -storetype pkcs12 -keystore keystore.p12
```

#### Existing Certificates

DigiCertCA.crt
acs.luminorgroup.com.key
acs_luminorgroup_com.crt

1)
    ```
     openssl pkcs12 -export -out keystore.pkcs12 -inkey acs.luminorgroup.com.key -in acs_luminorgroup_com.crt -certfile DigiCertCA.crt
   ```
2) ```
    keytool -v -importkeystore -srckeystore keystore.pkcs12 -srcstoretype PKCS12  -destkeystore keystore.p12 -storetype PKCS12
    ```


## Resources
* [Documentation in Confluence](https://confluence.luminorgroup.com/display/CARD/3DS+ACS)
* [Jira backlog](https://jira.luminorgroup.com/projects/BDBCDVIEU/issues)

## Configuration

There are 3 levels of configuration:
   
   #### Level 1 - Application internal
   Application internal (inside jar file) `application.properties`
   Some application internal properties, that no need to configure in most of the cases. 
   Here is example of some properties : 
   
   Endpoind name for translations:
   
   `app.api.translations.url=/api/translations`
    
   Cron expression for clean (Close Banktron session and delete from DB) unfinished and error user sessions. In this example every 15 minutes.
    
   `app.bgtask.clean-inactive-sessions.cron=0 0/15 * * * *`
   
   Attempts for change login and auth method:
   ```
   app.max-attempts.for-enter-login=5
   app.max-attempts.for-change-auth-method=5
   ```
   
   Translation file path and file name: 
   ```
    app.translations-filename=translations.json
    app.translations-path=.
   ```
   
   And some other properties.
   
   #### Level 2 - Application base properties
   Application base properties are external (outsize jar file) `application-base.properties`
   In there is base changes for all countries, but they may be changed from one ENV to another ENV, or may need to re-new some of them from time to time.
   
   This is SSL certificate for HTTPS connection, about how to build ti see **Existing Certificates** chapter.
   ```
    server.ssl.key-store=file:./keystore.p12
    server.ssl.key-store-password=dGFybGVrb24=
    server.ssl.key-store-type=pkcs12
   ```
   Inmemory database configuration: 
   ```
    spring.datasource.username=acs_auth_engine
    spring.datasource.password=J|7p$3L&qUCKHq>]S0<T
    spring.h2.console.enabled=false
   ```
   And hostname where application should run (need for frontend to make calls to "correct" URL)
   
   `app.application-host-base-url=https://test-acs.luminorgroup.com`
   
   #### Level 3 - Application country specific
   Application country specific properties are also external (outsize jar file) `application-country.properties`
   In this file defined some country specific links and properties:
   Here is some of them:
   
   Port on what application starts:
   
   `server.port=9708`
   
   Port where database starts (should be different for every country) :
   
   `spring.datasource.port=9091`

   Some application links:
   ```
    app.link-app.url=http://linker-pt2.dnb.lv:8080
    app.banktron.sonic.hostname=http://t1lv-sonicv.onelum.net
    app.banktron.sonic.port=2586
   ```

   Country settings (I think it clear what they mean without my description):
   ```
    app.language.default=lt
    app.language.allowed=lt, en, ru
    app.auth-method.allowed=SMART_ID, M_SIGNATURE, CODE_CALCULATOR
   ```
   
   **And last, but very important !!!** For what country run this application.
   
   `spring.profiles.active=lt`
   
   #### Configuration general information
   
   **Important !!!** Each property can be overridden on **each** level (including internal level). 
   
   If you define same property on **all 3 levels**, then value from **country specific property will be used**.
   
   This means that you can also override, for example, `app.max-attempts.for-enter-login=5` (from level 1 configuration) only for LV, or any other property for any other certain country.
   