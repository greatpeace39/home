// Never place permanent cloud credentials in frontend code.
// Uploads must use short-lived credentials from an authenticated backend.
export default {
  bucket: 'sto-itcast',
  region: 'oss-cn-beijing', // 例如 'oss-cn-beijing'
}
