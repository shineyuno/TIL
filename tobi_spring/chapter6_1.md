## 6.1 트랜잭션 코드의 분리

### 6.1.1 메소드 분리
트랜잭션 경계설정과 비즈니스 로직이 공존하는 메소드 
```java
public void upgradeLevels() throws Exception {
  
  TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition()); // 트랜잭션 시작
  try { //트랜잭션 안에서 진행되는 작업
    List<User> users = userDao.getAll();
    for(User user : users) {
      if(canUpgradeLevel(user)){
        upgradeLevel(user);
      }
    }
    transactionManager.commit(status); // 트랜잭션 커밋
  } catch(RuntimeException e) {
    transactionManager.rollback(status); //트랜잭션 커밋
    throw e;
  }
}
```


비즈니스 로직과 트랜잭션 경계설정의 분리
```java
public void upgradeLevels() throws Exception {
  
  TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition()); 
  try { 
    upgradeLevelsInternal()
    transactionManager.commit(status); 
  } catch(RuntimeException e) {
    transactionManager.rollback(status); 
    throw e;
  }
}

private void upgradeLevelsInternal() {  //분리된 비즈니스 로직 코드, 트랜잭션을 적용하기 전과 동일하다.
  List<User> users = userDao.getAll();
    for(User user : users) {
      if(canUpgradeLevel(user)){
        upgradeLevel(user);
      }
    }
}
```

### 6.1.2 DI를 이용한 클래스의 분리
트랜잭션이 적용된 UserServiceTx
``` java
public class UserServiceTx  implements UserService {
  UserService userService;
  PlatformTransactionManager transactionManager;
  
  public void setTransactionManager(PlatformTransactionManager transactionManager){
    this.transactionManager = transactionManager;
  }
  
  public void setUserService(UserService userService){
    this.userService = userSerivce;
  }
  
  public void add(User user){
    this.userService.add(user);
  }
  
  public void upgradeLevels() {
  
  TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition()); 
  try { 
    this.upgradeLevels()
    
    this.transactionManager.commit(status); 
  } catch(RuntimeException e) {
    this.transactionManager.rollback(status); 
    throw e;
  }
}
  
}
```
