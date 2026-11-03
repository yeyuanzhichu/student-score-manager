<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="工作台"/>
<c:set var="pageSubtitle" value="班级学生成绩总览"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<div class="hero">
  <h2>你好，<c:out value="${user.displayName}"/></h2>
  <p>欢迎回到成绩管理系统，以下是本学期（2026-2027-1）的整体数据概览。</p>
  <div class="hero-actions">
    <a class="btn" style="background:#fff;color:#1d4ed8" href="${ctx}/teacher/scores">
      <svg class="icon"><use href="#icon-scores"/></svg>去录成绩
    </a>
    <a class="btn" style="background:rgba(255,255,255,.16);color:#fff" href="${ctx}/teacher/reports">
      <svg class="icon"><use href="#icon-reports"/></svg>查看统计
    </a>
  </div>
</div>

<div class="stats-grid">
  <div class="stat-card">
    <div class="stat-top">
      <div class="stat-icon blue"><svg class="icon"><use href="#icon-students"/></svg></div>
    </div>
    <div class="stat-label">学生总数</div>
    <div class="stat-value">${dashboard.overall.studentCount}<small> 人</small></div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><div class="stat-icon teal"><svg class="icon"><use href="#icon-courses"/></svg></div></div>
    <div class="stat-label">课程数量</div>
    <div class="stat-value">${dashboard.overall.courseCount}<small> 门</small></div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><div class="stat-icon purple"><svg class="icon"><use href="#icon-clipboard"/></svg></div></div>
    <div class="stat-label">成绩记录</div>
    <div class="stat-value">${dashboard.overall.scoreCount}<small> 条</small></div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><div class="stat-icon green"><svg class="icon"><use href="#icon-reports"/></svg></div></div>
    <div class="stat-label">平均分</div>
    <div class="stat-value"><c:choose><c:when test="${not empty dashboard.overall.averageScore}"><fmt:formatNumber value="${dashboard.overall.averageScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose><small> 分</small></div>
  </div>
  <div class="stat-card">
    <div class="stat-top"><div class="stat-icon amber"><svg class="icon"><use href="#icon-award"/></svg></div></div>
    <div class="stat-label">及格率</div>
    <div class="stat-value"><c:choose><c:when test="${not empty dashboard.overall.passRate}"><fmt:formatNumber value="${dashboard.overall.passRate}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose><small> %</small></div>
  </div>
</div>

<div class="grid-2">
  <div class="card">
    <div class="card-header">
      <div><h3>班级成绩概览</h3><p class="sub">各班级平均分与及格率</p></div>
      <a class="btn btn-sm btn-secondary" href="${ctx}/teacher/reports">查看详情</a>
    </div>
    <div class="card-body no-pad">
      <div class="table-wrap">
        <table class="table">
          <thead><tr><th>班级</th><th class="num">人数</th><th class="num">成绩数</th><th class="num">平均分</th><th>及格率</th></tr></thead>
          <tbody>
          <c:forEach var="c" items="${dashboard.classStats}">
            <tr>
              <td class="strong"><c:out value="${c.className}"/></td>
              <td class="num">${c.studentCount}</td>
              <td class="num">${c.scoreCount}</td>
              <td class="num strong"><c:choose><c:when test="${not empty c.averageScore}"><fmt:formatNumber value="${c.averageScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></td>
              <td style="min-width:140px">
                <div style="display:flex;align-items:center;gap:10px">
                  <div class="progress" style="flex:1"><div class="progress-bar pb-green" style="width:${c.passRateWidth}%"></div></div>
                  <span class="badge ${c.passRate ge 85 ? 'badge-good' : (c.passRate ge 60 ? 'badge-fair' : 'badge-fail')}"><c:choose><c:when test="${not empty c.passRate}"><fmt:formatNumber value="${c.passRate}" maxFractionDigits="1" minFractionDigits="1"/>%</c:when><c:otherwise>-</c:otherwise></c:choose></span>
                </div>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-header"><h3>成绩分布</h3></div>
    <div class="card-body">
      <c:forEach var="d" items="${dashboard.distribution}">
        <div class="dist-item">
          <span class="dist-label"><c:out value="${d.label}"/></span>
          <div class="progress"><div class="progress-bar ${d.tone eq 'fail' ? 'pb-red' : (d.tone eq 'pass' ? 'pb-amber' : (d.tone eq 'fair' ? 'pb-teal' : (d.tone eq 'good' ? 'pb-green' : 'pb-purple')))}" style="width:${d.percentage}%"></div></div>
          <div><div class="dist-count">${d.count}</div><div class="dist-pct"><fmt:formatNumber value="${d.percentage}" maxFractionDigits="1" minFractionDigits="1"/>%</div></div>
        </div>
      </c:forEach>
    </div>
  </div>
</div>

<div class="card">
  <div class="card-header"><h3>课程成绩概览</h3></div>
  <div class="card-body no-pad">
    <div class="table-wrap">
      <table class="table">
        <thead><tr><th>课程</th><th>编号</th><th class="num">学分</th><th class="num">成绩数</th><th class="num">平均分</th><th class="num">最高</th><th class="num">最低</th><th>及格率</th></tr></thead>
        <tbody>
        <c:forEach var="co" items="${dashboard.courseStats}">
          <tr>
            <td class="strong"><c:out value="${co.courseName}"/></td>
            <td class="muted"><c:out value="${co.courseCode}"/></td>
            <td class="num"><fmt:formatNumber value="${co.credit}" maxFractionDigits="1" minFractionDigits="1"/></td>
            <td class="num">${co.scoreCount}</td>
            <td class="num strong"><c:choose><c:when test="${not empty co.averageScore}"><fmt:formatNumber value="${co.averageScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></td>
            <td class="num"><c:choose><c:when test="${not empty co.highestScore}"><fmt:formatNumber value="${co.highestScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></td>
            <td class="num"><c:choose><c:when test="${not empty co.lowestScore}"><fmt:formatNumber value="${co.lowestScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></td>
            <td><span class="badge ${co.passRate ge 85 ? 'badge-good' : (co.passRate ge 60 ? 'badge-fair' : 'badge-fail')}"><c:choose><c:when test="${not empty co.passRate}"><fmt:formatNumber value="${co.passRate}" maxFractionDigits="1" minFractionDigits="1"/>%</c:when><c:otherwise>-</c:otherwise></c:choose></span></td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</div>

<div class="card">
  <div class="card-header"><h3>最近成绩更新</h3></div>
  <div class="card-body no-pad">
    <div class="table-wrap">
      <table class="table">
        <thead><tr><th>学生</th><th>学号</th><th>班级</th><th>课程</th><th class="num">成绩</th><th>更新时间</th></tr></thead>
        <tbody>
        <c:forEach var="r" items="${dashboard.recentScores}">
          <tr>
            <td class="strong"><c:out value="${r.studentName}"/></td>
            <td class="muted"><c:out value="${r.studentNo}"/></td>
            <td><c:out value="${r.className}"/></td>
            <td><c:out value="${r.courseName}"/></td>
            <td class="num strong"><span class="score-pill ${r.score ge 90 ? 'score-excellent' : (r.score ge 80 ? 'score-good' : (r.score ge 70 ? 'score-fair' : (r.score ge 60 ? 'score-pass' : 'score-fail')))}"><fmt:formatNumber value="${r.score}" maxFractionDigits="1" minFractionDigits="1"/></span></td>
            <td class="muted"><fmt:formatDate value="${r.updatedAt}" pattern="yyyy-MM-dd HH:mm"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty dashboard.recentScores}">
          <tr><td colspan="6" class="muted" style="text-align:center;padding:28px">暂无成绩记录</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
