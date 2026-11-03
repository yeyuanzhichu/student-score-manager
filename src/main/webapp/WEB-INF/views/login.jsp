<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>登录 · 智汇成绩</title>
<link rel="icon" href="${ctx}/assets/img/favicon.svg" type="image/svg+xml">
<link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body class="login-body">
<div class="login-wrap">
  <aside class="login-hero">
    <div class="brand">
      <div class="brand-logo">智</div>
      <div style="font-size:20px;font-weight:700">智汇成绩</div>
    </div>
    <h1>让每一份成绩<br>都有清晰的成长轨迹</h1>
    <p>面向班级的轻量成绩管理系统，覆盖教师成绩录入、学生自主查询、平均分与及格率统计分析。</p>
    <ul class="login-features">
      <li><span class="dot"></span>教师端：学生 / 课程 / 成绩全流程管理</li>
      <li><span class="dot"></span>学生端：随时查询个人成绩与班级对比</li>
      <li><span class="dot"></span>一键统计平均分、及格率、优秀率</li>
      <li><span class="dot"></span>本地文件数据库，开箱即用</li>
    </ul>
  </aside>

  <main class="login-panel">
    <div class="login-brand-sm">
      <div class="brand-logo">智</div>
      <div><strong>智汇成绩</strong></div>
    </div>

    <div class="login-card">
      <h1>欢迎回来</h1>
      <p class="login-sub">登录班级学生成绩管理系统</p>

      <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
      </c:if>
      <c:if test="${not empty info}">
        <div class="alert alert-info"><c:out value="${info}"/></div>
      </c:if>

      <form class="login-form" method="post" action="${ctx}/login" autocomplete="off">
        <div class="form-group">
          <label class="form-label" for="account">账号 <span class="req">*</span></label>
          <input class="form-control" id="account" name="account" type="text"
                 placeholder="教师账号或学生学号" value="<c:out value='${account}'/>" required>
        </div>
        <div class="form-group">
          <label class="form-label" for="password">密码 <span class="req">*</span></label>
          <input class="form-control" id="password" name="password" type="password"
                 placeholder="请输入密码" required>
        </div>
        <button class="btn btn-primary btn-block" type="submit">登 录</button>
      </form>
    </div>

    <div class="login-demo">
      演示账号：教师 <code>teacher / 123456</code> · 学生 <code>2024010101 / 123456</code>
      <div class="demo-btns">
        <button type="button" class="demo-btn" data-account="teacher" data-password="123456">教师登录</button>
        <button type="button" class="demo-btn" data-account="2024010101" data-password="123456">学生登录</button>
      </div>
    </div>
    <div class="login-foot">Java EE 课程设计 · Jakarta Servlet + JSP + H2</div>
  </main>
</div>
<script src="${ctx}/assets/js/app.js"></script>
</body>
</html>
