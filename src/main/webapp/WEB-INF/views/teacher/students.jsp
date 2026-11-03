<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="学生管理"/>
<c:set var="pageSubtitle" value="维护班级学生档案，学生凭学号登录查询成绩"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="toolbar">
  <h2>学生列表 <span class="badge badge-info">${fn:length(students)} 人</span></h2>
  <a class="btn btn-primary" href="${ctx}/teacher/student">
    <svg class="icon"><use href="#icon-plus"/></svg>新增学生
  </a>
</div>

<form class="card" method="get" action="${ctx}/teacher/students">
  <div class="filter-bar">
    <div class="form-group grow">
      <label class="form-label" for="keyword">关键词</label>
      <input class="form-control" id="keyword" name="keyword" type="text"
             placeholder="按姓名 / 学号 / 电话搜索" value="<c:out value='${keyword}'/>">
    </div>
    <div class="form-group">
      <label class="form-label" for="classId">班级</label>
      <select class="form-select" id="classId" name="classId">
        <option value="">全部班级</option>
        <c:forEach var="c" items="${classes}">
          <option value="${c.id}" ${c.id eq classId ? 'selected' : ''}><c:out value="${c.className}"/></option>
        </c:forEach>
      </select>
    </div>
    <button class="btn btn-primary" type="submit"><svg class="icon"><use href="#icon-search"/></svg>查询</button>
    <a class="btn btn-ghost" href="${ctx}/teacher/students">重置</a>
  </div>
</form>

<div class="card">
  <div class="card-body no-pad">
    <div class="table-wrap">
      <table class="table">
        <thead><tr><th>学号</th><th>姓名</th><th>性别</th><th>班级</th><th>联系电话</th><th class="num">已录成绩</th><th class="num">平均分</th><th style="width:180px">操作</th></tr></thead>
        <tbody>
        <c:forEach var="s" items="${students}">
          <tr>
            <td class="strong"><c:out value="${s.studentNo}"/></td>
            <td><c:out value="${s.name}"/></td>
            <td><span class="badge ${s.gender eq '男' ? 'badge-info' : 'badge-excellent'}"><c:out value="${s.gender}"/></span></td>
            <td><c:out value="${s.className}"/></td>
            <td class="muted"><c:out value="${s.phone}"/></td>
            <td class="num">${s.scoreCount}</td>
            <td class="num strong"><c:choose><c:when test="${not empty s.averageScore}"><fmt:formatNumber value="${s.averageScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></td>
            <td>
              <a class="btn btn-sm btn-secondary" href="${ctx}/teacher/student?id=${s.id}">
                <svg class="icon"><use href="#icon-edit"/></svg>编辑
              </a>
              <form method="post" action="${ctx}/teacher/student/delete" data-confirm="确认删除学生「${s.name}」？其成绩记录将一并删除。" style="display:inline">
                <input type="hidden" name="id" value="${s.id}">
                <button class="btn btn-sm btn-outline-danger" type="submit">
                  <svg class="icon"><use href="#icon-trash"/></svg>删除
                </button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty students}">
          <tr><td colspan="8" class="muted" style="text-align:center;padding:40px">未找到匹配的学生</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
