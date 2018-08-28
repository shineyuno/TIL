## application.mode

Application mode (case insensitive). For example:
```
application.mode=prod
```
Values:

`DEV` - 동적으로 소스 변화를 reloading(재컴파일) 해서 반영한다.
`PROD` - pre-compiles and caches Java sources and templates.
Default: DEV
