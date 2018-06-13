# Project : Electric wheel Redesign

- 기존 전동휠의 인간공학적 문제에 대한 재설계


### 프로젝트 목적 및 기여

- Android Project
- 하드웨어인 EV3 와 유저의 인터페이스 구현
- 전동휠 재설계 및 지문인식 시스템 구현
- 전동휠의 위치 추적 시스템 구현

### 시나리오

1. 유저가 전동휠 전원 on/off 할 수 있는 App 실행
2. App 에 등록된 지문으로만 인증 가능
3. 인증 성공시, 전동휠 전원 on
4. 도난 방지를 위한 전동휠의 위치 추적

### 구현 내용

#### 1. 지문 인식 시스템
- [시연 동영상](https://drive.google.com/open?id=1FBZhl9l7q9Jy9ltntRIzv982qANR_nVb)

1. Android Manifest 파일에 지문 권한 추가

~~~
<uses-permission android:name="android.permission.USE_FINGERPRINT"/>
~~~


2. 지문인증을 위한 키, 암호 및 암호화 개체의 관점에서 지문 인증 준비 및 암호 초기화
~~~
keyguardManager =
               (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
fingerprintManager =
               (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
~~~

3. Fingerprint Manager의 authenticate 메소드 호출을 통해 지문 인증 성공 여부 반환

4. 인증 성공시, intent 로 ev3 app 실행
~~~
ComponentName componentName = new ComponentName("com.lego.mindstorms.robotcommander",
                    "com.lego.lms.ev3.retail.LaunchActivity");
Intent intent = new Intent(Intent.ACTION_MAIN);
intent.addCategory(Intent.CATEGORY_LAUNCHER);
intent.setComponent(componentName);
startActivity(intent);
~~~

![image](https://user-images.githubusercontent.com/33097467/41368021-a1a9473e-6f7b-11e8-9bdb-0283471babb0.png)![image](https://user-images.githubusercontent.com/33097467/41367900-5719bd52-6f7b-11e8-8ec9-53516d757eb6.png)
#### 2.위치 추적 시스템

> Google Map API 사용하여 지도에 현재위치 표시
- [시연 동영상](https://drive.google.com/open?id=1DkMdmC2VUODfZumYJOWUuBnYV9vy5mEd)

![image](https://user-images.githubusercontent.com/33097467/41367771-f3f9285c-6f7a-11e8-954b-98a7143bfa3f.png)
