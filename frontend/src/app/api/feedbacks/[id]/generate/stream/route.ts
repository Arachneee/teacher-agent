import { NextRequest } from 'next/server';

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;
  const backendUrl = process.env.API_URL || 'http://localhost:8080';

  const body = await request.json().catch(() => ({}));

  const res = await fetch(`${backendUrl}/feedbacks/${id}/generate/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      cookie: request.headers.get('cookie') ?? '',
    },
    body: JSON.stringify({ instruction: body.instruction ?? null }),
  });

  if (!res.ok) {
    return new Response('AI 문자를 생성하지 못했어요', { status: res.status });
  }

  return new Response(res.body, {
    headers: {
      'Content-Type': 'text/plain; charset=utf-8',
      'Cache-Control': 'no-cache',
      'X-Accel-Buffering': 'no',
    },
  });
}
