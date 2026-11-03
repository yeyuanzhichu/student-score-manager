<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="我的成绩"/>
<c:set var="pageSubtitle" value="个人成绩单与班级对比"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="hero">
  <h2><c:out value="${user.displayName}"/>，你的成绩单</h2>
  <p><c:out value="${classInfo.major}"/> · <c:out value="${classInfo.className}"/> · 学号 <c:out value="${user.studentNo}"/></p>
  <div class="hero-actions">
    <button id="printBtn" class="btn" style="background:#fff;color:#1d4ed8">
      <svg class="icon"><use href="#icon-print"/></svg>打印成绩单
    </button>
  </div>
</div>

<div class="stats-grid" style="grid-template-columns:repeat(4,1fr)">
  <div class="stat-card">
    <div class="stat-top"><div class="stat-icon blue"><svg class="icon"><use href="#icon-reports"/></svg></div></div>
    <div class="stat-label">平均分</div>
    <div class="stat-value"><c:choose><c:when test="${not empty view.summary.averageScore}"><fmt:formatNumber value="${view.summary.averageScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose><small> 分</small></div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><div class="stat-icon green"><svg class="icon"><use href="#icon-scores"/></svg></div></div>
    <div class="stat-label">及格率</div>
    <div class="stat-value"><c:choose><c:when test="${not empty view.summary.passRate}"><fmt:formatNumber value="${view.summary.passRate}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose><small> %</small></div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><div class="stat-icon teal"><svg class="icon"><use href="#icon-award"/></svg></div></div>
    <div class="stat-label">已获学分</div>
    <div class="stat-value"><fmt:formatNumber value="${view.summary.earnedCredits}" maxFractionDigits="1" minFractionDigits="1"/><small> / <fmt:formatNumber value="${view.summary.totalCredits}" maxFractionDigits="1" minFractionDigits="1"/></small></div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><div class="stat-icon purple"><svg class="icon"><use href="#icon-students"/></svg></div></div>
    <div class="stat-label">班级排名</div>
    <div class="stat-value">${view.summary.rank}<small> 名</small></div>
  </div>
</div>

<div class="card">
  <div class="card-header">
    <div><h3>课程成绩明细</h3><p class="sub">共 ${fn:length(view.scores)} 门课程</p></div>
    <span class="badge badge-${view.summary.levelTone}">${view.summary.level}</span>
  </div>
  <div class="card-body no-pad">
    <div class="table-wrap">
      <table class="table">
        <thead><tr><th>课程</th><th>编号</th><th class="num">学分</th><th>任课教师</th><th class="num">成绩</th><th class="num">班级平均</th><th class="num">与班均差</th><th class="num">班级排名</th><th>状态</th></tr></thead>
        <tbody>
        <c:forEach var="sc" items="${view.scores}">
          <tr>
            <td class="strong"><c:out value="${sc.courseName}"/></td>
            <td class="muted"><c:out value="${sc.courseCode}"/></td>
            <td class="num"><fmt:formatNumber value="${sc.credit}" maxFractionDigits="1" minFractionDigits="1"/></td>
            <td><c:out value="${sc.teacherName}"/></td>
            <td class="num strong"><span class="score-pill ${sc.score ge 90 ? 'score-excellent' : (sc.score ge 80 ? 'score-good' : (sc.score ge 70 ? 'score-fair' : (sc.score ge 60 ? 'score-pass' : 'score-fail')))}"><fmt:formatNumber value="${sc.score}" maxFractionDigits="1" minFractionDigits="1"/></span></td>
            <td class="num"><c:choose><c:when test="${not empty sc.classAverage}"><fmt:formatNumber value="${sc.classAverage}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></td>
            <td class="num"><c:choose>
              <c:when test="${sc.difference gt 0}"><span style="color:#16a34a">+<fmt:formatNumber value="${sc.difference}" maxFractionDigits="1" minFractionDigits="1"/></span></c:when>
              <c:when test="${sc.difference lt 0}"><span style="color:#dc2626"><fmt:formatNumber value="${sc.difference}" maxFractionDigits="1" minFractionDigits="1"/></span></c:when>
              <c:otherwise><span class="muted">0</span></c:otherwise>
            </c:choose></td>
            <td class="num">${sc.classRank} / ${sc.classSize}</td>
            <td>
              <c:choose>
                <c:when test="${sc.score ge 90}"><span class="badge badge-excellent">优秀</span></c:when>
                <c:when test="${sc.score ge 80}"><span class="badge badge-good">良好</span></c:when>
                <c:when test="${sc.score ge 70}"><span class="badge badge-fair">中等</span></c:when>
                <c:when test="${sc.score ge 60}"><span class="badge badge-pass">及格</span></c:when>
                <c:otherwise><span class="badge badge-fail">不及格</span></c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty view.scores}">
          <tr><td colspan="9" class="muted" style="text-align:center;padding:40px">暂无成绩记录，请等待任课教师录入</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
  <div class="card-body">
    <p class="form-hint" style="margin:0">说明：成绩由任课教师录入，如有疑问请联系教务处；与班均差为正表示高于班级平均分。</p>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
