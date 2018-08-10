# cherry-pick

다른 브랜치의 전체 commit 내역을 복사해오지 않고 특정 commit 내역만을 가져오고 싶은 경우 사용한다.

## 오늘 사용한 경위
작업 하던 브랜치에  다른 브랜치를 merge 해버려서  git commit 내역이(히스토리?) 꼬여버렸다.
master 에 작업 브랜치를 풀리퀘를 하려고하니 내가 작업한 내용말고 다른브랜치 내용이 합쳐져있어 컨플릭트도 나고 할수없었다.

그래서 master에서  새로운 브랜치를 따고 기존 작업 브랜치에서 내가 작업한 부분만을 복사해와 작업했다.

작업 히스토리 
```bash
git checkout master  #마스터로 이동
git pull  #master 작업 내역을 받음
git branch task/681-story-app-http-build-2  #새로운 브랜치 땀
git checkout task/681-story-app-http-build-2  #새로운 브랜치로 이동
git cherry-pick f89597e   # 기존 작업 브랜치 이름은 task/681-story-app-http-build 이었는데 여기서 커밋한 키번호 "f89597e" 로 변경내용 가져옴
git cherry-pick d178e10   #위와 마찬가지로 기존 작업브랜치에 쓸 커밋내역만 복사 
git cherry-pick 28324ad   #위와 마찬가지로 기존 작업브랜치에 쓸 커밋내역만 복사 
git log                   #git 히스토리 확인
git push origin task/681-story-app-http-build-2   #잘된거 확인후 푸쉬!!
```
