<%@ page contentType="text/html;charset=UTF-8" isErrorPage="false" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>页面未找到 · 智汇成绩</title>
<link rel="icon" href="${ctx}/assets/img/favicon.svg" type="image/svg+xml">
<link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body style="display:grid;place-items:center;min-height:100vh">
<div class="empty-state">
  <div style="font-size:72px;font-weight:800;color:#2563eb">404</div>
  <h3>页面未找到</h3>
  <p>你访问的页面不存在或已被移除。</p>
  <a class="btn btn-primary" href="${ctx}/login" style="margin-top:16px">返回登录</a>
</div>
</body>
</html>
