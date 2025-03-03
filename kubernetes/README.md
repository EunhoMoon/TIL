# Kubernetes

- 컨테이너 배포를 관리(설명)하고 오케스트레이션 하기 위한 표준
- 자동 배포, 스케일링, 로드 밸런싱, 모니터링 및 관리 등

## Architecture

<img src="./kubernetes.png">

- **Pod(Container)**: 가장 작은 단위, 컨테이너를 보유 및 실행
- **Worker Node**: pod가 컨테이너를 실행하는 곳, 하나 이상의 worker node가 필요(일반 적으로 둘 이상 존재)
- **Proxy/Config**: worker node에서 pod 및 pod 내부의 컨테이너의 외부 통신 제어
- **Master Node**
  - Control plane: worker node와 상호 작용하며 제어, master node에서 실행되는 다양한 서비스의 도구 모음
- **Cluster**: 위 모든 것은 포함한 단위, 하나의 네트워크를 형성

### Worker Node

- master node에 의해 관리되는 물리적 머신, 가상 머신 등의 실행 환경
- 한 개 이상의 pod를 포함하고 있으며, pod는 하나 이상의 어플리케이션 컨테이너와 속한 리소스(config, volume 등)를 관리하고 호스팅한다.
- Pod는 Kubernetes에 의해 관리되며 Kubernetes는 pod를 생성/삭제 가능

#### kublet

- worker node와 master node의 통신을 위한 소프트웨어
- master node가 worker node를 제어할 수 있도록 한다.

#### kube-proxy

- 네트워크 트래픽을 처리
- 인바운드/아웃바운드에 대한 허용/거부

### Master Node

- API server, scheduler, controller-manager를 보유하고 있으며, 이들은 서로 긴밀하게 연동되어 작동

#### API Server

- worker node에서 실행되는 kublet과 통신을 수행

#### Scheduler

- pod를 모니터링 하고, 새 pod를 생성할 worker node를 선택(장애 대응 및 스케일링)
- 위 내용을 토대로 worker node가 어떤 동작을 해야할지 API server에 알리는 역할

#### Controller-Manager

- worker node 전체에 대한 모니터링 및 제어
- 적당한 수의 pod를 가동중인지 확인
- Kube-controller-manager와 Cloud-controller-manager가 존재

### Deployment

- 생성하고 관리해야 하는 pod의 수와 컨테이너 수에 대한 지침을 제공
  1.  목표 상태를 지정하면 Kubernetes가 해당 상태에 도달하기 위한 작업을 수행
      - pod 생성 및 worker node에 배치
  2.  배포를 일시 중지하거나 삭제, 혹은 rollback할 수 있다.
  3.  배포에 대한 스케일링 가능
      - pod 스케일링 및 matric(CPU, memory, 트래픽 등...)을 통한 auto scaling 가능
- 일반적으로 pod를 직접 관리하는 것이 아닌, Deployment Object를 통해 관리한다.
- template 속성을 통해 pod를 생성하고, 해당 내용이 변경시 업데이트 진행
  - 업데이트 시 새로운 이름의 replica set을 생성하고, 기존 replica set의 pod를 삭제
  - rollback 시 이전 replica set으로 pod 복구

#### strategy

- Recreate
  - 기존 pod를 삭제하는 동시에 새로운 pod를 생성
  - downtime이 발생할 수 있다.
- RollingUpdate
  - 새로운 pod를 생성하고 기존 pod를 삭제
  - downtime이 발생하지 않지만 자원 사용량이 150% 증가한다.
  - 업데이트 동안 두 버전이 동시에 호출될 수 있다.
    - `maxUnavailable`: 업데이트 중 중단할 수 있는 pod의 최대 비율
    - `maxSurge`: 업데이트 중 새로 생성할 수 있는 pod의 최대 비율, 성공적으로 업데이트 된 Pod 수 만큼 기존 Pod를 삭제
- Blue/Green
  - 새로운 pod를 생성하고 기존 pod를 삭제
  - downtime이 발생하지 않지만 자원 사용량이 200% 증가한다.
  - 업데이트 동안 두 버전이 동시에 호출되지 않는다.
  - 별도 배포 솔루션에서 지원

### Service

- pod를 클러스터 안의 다른 pod에 노출하거나 외부로 노출
- 서비스 없이는 외부 혹은 pod에서 pod에 접근하기 매우 어렵다.
- pod를 그룹화하고, 공유 IP 주소를 제공(pod가 변경되어도 공유 IP 주소는 변경되지 않는다.)
- type
  - ClusterIP: 클러스터 내부에서만 사용 가능, 기본값
    - Kubernetes 환경 변수 사용(예: `[SERVICE_NAME]\_SERVICE_HOST`)
    - DNS 사용(예: `[service-name].[namespace].svc.cluster.local`)
  - NodePort: 외부에서 접근 가능, Node의 IP와 Port를 통해 접근 가능
  - LoadBalancer: 외부에서 접근 가능, 클라우드 서비스의 로드 밸런서를 사용하여 외부로 노출

### Kubernetes Volumes

- Docker의 Volume 시스템을 사용하지만 더 많은 기능과 구성 옵션을 가지고 있다.
- Pod 단위에서 공유 가능(같은 Pod 내의 여러 컨테이너가 하나의 Volume을 공유 가능)
- Volume의 수명은 Pod의 수명과 동일(PersistentVolume 사용시 Pod와 관계없이 유지 가능)
- 다양한 Volume 유형 지원원

#### PersistentVolume(영구 볼륨)

- Pod가 아닌 Kubernetes Cluster 리소스
- 따라서 Pod 및 노드에 의존하지 않으며, Pod 수명 주기와 완전히 분리된 수명 주기를 갖는다.
- Pod는 내부에 PersistentVolume Claim을 두고고, 이를 통해 PersistentVolume에 엑세스할 수 있다.
- 인프라 담당자가 관리하도록 설계

#### PersistentVolumeClaim

- Pod에서 필요한 PersistentVolume을 요청하는 방법
- 개발자가 관리하도록 설계
- 이러한 인터페이스를 통해 PV 솔루션의 변경이 이루어지더라도 Pod는 변경 없이 사용 가능

### HPA(Horizontal Pod Autoscaler)

- Pod의 수를 자동으로 조정
  - `scaleTargetRef`: 대상 Deployment의 이름
  - `minReplicas`: 최소 Pod 수
  - `maxReplicas`: 최대 Pod 수
  - `metrics`: Pod의 상태를 측정하는 방법
    - `type`: 사용할 메트릭 유형
    - `resource`: 메트릭 `type`이 `Resource`인 경우 사용, CPU, Memory 등
    - `object`: 메트릭 `type`이 `Object`인 경우 사용
    - `external`: 메트릭 `type`이 `External`인 경우 사용
  - `behavior`: 잦은 스케일링을 방지하기 위한 설정
    - `scaleDown`, `scaleUp`: 스케일링을 얼마나 빠르게 할지 결정
      - `stabilizationWindowSeconds`: 특정 시간 동안 조건 유지 시 스케일링
      - `policies`: 스케일링 정책, `type`과 `value`로 구성
    ```yaml
    behavior:
      scaleDown: # 스케일 다운 설정
        stabilizationWindowSeconds: 300 # 5분 동안 조건 유지 시 스케일링
        policies: # 1분에 1개씩 스케일링
          - type: Pods
            value: 1
            periodSeconds: 60
    ```

### Kubernetes Probe

- Application의 상태를 확인하고, 상태에 따라 Pod의 상태를 변경(재시작, 중지, 외부 노출 등)
- Probe Type
  - Startup Probe: Pod가 시작되었는지 확인(App health check)
  - Liveness Probe: Pod가 정상적으로 작동하는지 확인(요청을 처리할 준비가 되었는지 확인, 외부 노출은 하지 않는 상태)
  - Readiness Probe: Pod가 요청을 처리할 준비가 되었는지 확인(외부 노출을 위한 상태)
- Probe를 위한 API는 반복적으로 호출되어야 하기 때문에 가볍게 구성해야 한다.
