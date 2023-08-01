function FindProxyForURL(url, host) {
  // 检查是否是 www.googleapis.com 域名
  if (host == "www.googleapis.com") {
    return "PROXY 192.168.10.104:7890";
  }
  return "DIRECT";
}
