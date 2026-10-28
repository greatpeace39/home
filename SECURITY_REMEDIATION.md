# Security configuration

Permanent Alibaba Cloud credentials have been removed from the frontend configuration.
Never put long-lived AccessKeys in browser code, including frontend environment variables.
If cloud uploads are needed, use authenticated backend uploads or scoped, short-lived STS credentials.

Set DB_PASSWORD and JWT_SECRET outside the repository before starting the Java application.
There are intentionally no committed fallback passwords or JWT signing secrets.
Use a new random JWT secret of at least 32 bytes and rotate any credentials reused elsewhere.

Recording uploads use server-generated filenames and never overwrite an existing file.
Configure app.recordings.upload-dir for the storage directory.
This legacy demo does not implement upload authentication, quotas, or audio-content inspection;
add those controls before exposing the endpoint to untrusted users.

Earlier commits can still contain removed credentials. A normal cleanup commit does not
erase Git history, forks, caches, or copies. Previously exposed credentials must remain revoked.
