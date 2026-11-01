const blockedRequestHeaders = new Set([
  'connection',
  'content-length',
  'host',
  'transfer-encoding'
]);

const blockedResponseHeaders = new Set([
  'connection',
  'content-encoding',
  'content-length',
  'transfer-encoding'
]);

export default async function handler(request) {
  const configuredBase = process.env.SCRAMBLED_API_BASE_URL;
  if (!configuredBase) {
    return Response.json(
      { message: 'The recipe API is not configured. Set SCRAMBLED_API_BASE_URL in Netlify.' },
      { status: 503 }
    );
  }

  let baseUrl;
  try {
    baseUrl = new URL(configuredBase);
  } catch {
    return Response.json(
      { message: 'SCRAMBLED_API_BASE_URL must be a valid backend URL.' },
      { status: 500 }
    );
  }
  if (!['https:', 'http:'].includes(baseUrl.protocol)
      || (baseUrl.protocol === 'http:' && !['localhost', '127.0.0.1'].includes(baseUrl.hostname))
      || baseUrl.pathname !== '/' || baseUrl.search || baseUrl.hash) {
    return Response.json(
      { message: 'Configure the backend as an HTTPS origin without a path, query, or fragment.' },
      { status: 500 }
    );
  }

  const incomingUrl = new URL(request.url);
  const apiPath = incomingUrl.pathname
    .replace(/^\/\.netlify\/functions\/api(?=\/|$)/, '/api');
  const upstreamUrl = new URL(`${apiPath}${incomingUrl.search}`, baseUrl);
  const headers = new Headers(request.headers);
  for (const header of blockedRequestHeaders) headers.delete(header);

  try {
    const upstream = await fetch(upstreamUrl, {
      method: request.method,
      headers,
      body: ['GET', 'HEAD'].includes(request.method) ? undefined : await request.arrayBuffer(),
      redirect: 'manual'
    });
    const responseHeaders = new Headers();
    upstream.headers.forEach((value, name) => {
      if (!blockedResponseHeaders.has(name.toLowerCase()) && name.toLowerCase() !== 'set-cookie') {
        responseHeaders.append(name, value);
      }
    });
    const cookies = upstream.headers.getSetCookie?.() || [];
    for (const cookie of cookies) responseHeaders.append('set-cookie', cookie);
    if (!cookies.length && upstream.headers.has('set-cookie')) {
      responseHeaders.set('set-cookie', upstream.headers.get('set-cookie'));
    }

    return new Response(upstream.body, { status: upstream.status, headers: responseHeaders });
  } catch (error) {
    console.error('Recipe API proxy request failed.', error);
    return Response.json({ message: 'The recipe API could not be reached.' }, { status: 502 });
  }
}
