# Frontend 배포 가이드 (Vercel)

## 개요

Next.js 프론트엔드는 Vercel에 배포된다. GitHub 저장소와 연동하여 main 브랜치 push 시 자동으로 빌드 및 배포된다.

## 환경변수

Vercel 프로젝트 설정에서 아래 환경변수를 설정해야 한다.

| 변수 | 설명 | 예시 |
|------|------|------|
| `API_URL` | 백엔드 EC2 주소 | `http://<EC2_HOST>:8080` |

`API_URL`이 없으면 `next.config.ts`의 rewrite 규칙에 의해 `http://localhost:8080`이 기본값으로 사용된다.

## API 프록시 설정

`frontend/next.config.ts`에서 `/api/*` 경로를 백엔드로 프록시한다.

```ts
async rewrites() {
  const backendUrl = process.env.API_URL || 'http://localhost:8080';
  return [
    {
      source: '/api/:path*',
      destination: `${backendUrl}/:path*`,
    },
  ];
}
```

프론트엔드에서 `/api/...`로 요청하면 백엔드 EC2로 전달된다. CORS 문제 없이 통신 가능하다.

## 배포 절차

### 최초 설정

1. [vercel.com](https://vercel.com)에서 프로젝트 생성
2. GitHub 저장소 연결
3. **Root Directory**: `frontend` 로 설정
4. 환경변수 `API_URL` 등록
5. Deploy

### 이후 배포

`frontend/**` 경로 변경이 main에 push되면 Vercel이 자동으로 빌드 및 배포한다.

## 로컬 빌드 확인

```bash
cd frontend
npm run build   # 프로덕션 빌드
npm run lint    # ESLint 검사
```
