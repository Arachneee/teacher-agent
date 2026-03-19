# Frontend Deployment Guide (Vercel)

## Purpose
This document provides instructions for deploying the Next.js frontend application to Vercel, including necessary configurations and deployment steps.

## Overview
The Next.js frontend is deployed on Vercel. The deployment is automated via integration with the GitHub repository; pushes to the `main` branch automatically trigger builds and deployments.

## Environment Variables

The following environment variables must be configured in the Vercel project settings:

| Variable  | Description                           | Example                      |
| :-------- | :------------------------------------ | :--------------------------- |
| `API_URL` | Backend EC2 instance address          | `http://<EC2_HOST>:8080`     |

If `API_URL` is not provided, the `next.config.ts` rewrite rule defaults to `http://localhost:8080`.

## API Proxy Configuration

The `frontend/next.config.ts` file is configured to proxy requests starting with `/api/*` to the backend.

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

Requests made from the frontend to `/api/...` will be proxied to the backend EC2. This ensures communication without CORS issues.

## Deployment Process

### Initial Setup on Vercel
1.  Create a new project on [vercel.com](https://vercel.com).
2.  Connect your GitHub repository.
3.  Set the **Root Directory** to `frontend`.
4.  Configure the environment variable `API_URL`.
5.  Click the **Deploy** button.

### Subsequent Deployments
Any changes pushed to the `main` branch affecting files within the `frontend/**` path will automatically trigger a build and deployment by Vercel.

## Local Build Verification

Before deploying or pushing changes, you can verify the build and linting locally:

```bash
cd frontend
npm run build   # Runs the production build
npm run lint    # Runs ESLint checks
```
