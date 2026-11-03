<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="课程管理"/>
<c:set var="pageSubtitle" value="维护课程信息与任课教师"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="toolbar">
  <h2>课程列表 <span class="badge badge-info">${fn:length(courses)} 门</span></h2>
  <a class="btn btn-primary" href="${ctx}/teacher/course">
    <svg class="icon"><use href="#icon-plus"/></svg>新增课程
  </a>
</div>

<div class="card">
  <div class="card-body no-pad">
    <div class="table-wrap">
      <table class="table">
        <thead><tr><th>课程编号</th><th>课程名称</th><th class="num">学分</th><th>任课教师</th><th>学期</th><th class="num">已录成绩</th><th class="num">平均分</th><th style="width:180px">操作</th></tr></thead>
        <tbody>
        <c:forEach var="co" items="${courses}">
          <tr>
            <td class="muted strong"><c:out value="${co.courseCode}"/></td>
            <td class="strong"><c:out value="${co.courseName}"/></td>
            <td class="num"><fmt:formatNumber value="${co.credit}" maxFractionDigits="1" minFractionDigits="1"/></td>
            <td><c:out value="${co.teacherName}"/></td>
            <td><span class="badge badge-muted"><c:out value="${co.semester}"/></span></td>
            <td class="num">${co.scoreCount}</td>
            <td class="num strong"><c:choose><c:when test="${not empty co.averageScore}"><fmt:formatNumber value="${co.averageScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></td>
            <td>
              <a class="btn btn-sm btn-secondary" href="${ctx}/teacher/course?id=${co.id}">
                <svg class="icon"><use href="#icon-edit"/></svg>编辑
              </a>
              <form method="post" action="${ctx}/teacher/course/delete" data-confirm="确认删除课程「${co.courseName}」？" style="display:inline">
                <input type="hidden" name="id" value="${co.id}">
                <button class="btn btn-sm btn-outline-danger" type="submit">
                  <svg class="icon"><use href="#icon-trash"/></svg>删除
                </button>
              </form>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty courses}">
          <tr><td colspan="8" class="muted" style="text-align:center;padding:40px">暂无课程，点击右上角「新增课程」添加</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
