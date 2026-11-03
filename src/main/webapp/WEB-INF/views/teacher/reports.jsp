<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="统计分析"/>
<c:set var="pageSubtitle" value="平均分、及格率、优秀率与成绩分布"/>
<%@ include file="/WEB-INF/views/common/header.jspf" %>

<form class="card" method="get" action="${ctx}/teacher/reports">
  <div class="filter-bar">
    <div class="form-group grow">
      <label class="form-label" for="classId">选择班级</label>
      <select class="form-select" id="classId" name="classId">
        <c:forEach var="c" items="${report.classes}">
          <option value="${c.id}" ${c.id eq report.selectedClass.id ? 'selected' : ''}><c:out value="${c.className}"/></option>
        </c:forEach>
      </select>
    </div>
    <button class="btn btn-primary" type="submit"><svg class="icon"><use href="#icon-search"/></svg>查看统计</button>
    <a class="btn btn-secondary" href="${ctx}/teacher/export?classId=${report.selectedClass.id}">
      <svg class="icon"><use href="#icon-download"/></svg>导出本班成绩
    </a>
  </div>
</form>

<c:if test="${not empty report.selectedClass}">
  <div class="grid-3" style="margin-bottom:20px">
    <div class="mini-stat">
      <div class="value">${report.classStat.studentCount}</div>
      <div class="label">学生人数</div>
    </div>
    <div class="mini-stat">
      <div class="value"><c:choose><c:when test="${not empty report.classStat.averageScore}"><fmt:formatNumber value="${report.classStat.averageScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></div>
      <div class="label">班级平均分</div>
    </div>
    <div class="mini-stat">
      <div class="value"><c:choose><c:when test="${not empty report.classStat.passRate}"><fmt:formatNumber value="${report.classStat.passRate}" maxFractionDigits="1" minFractionDigits="1"/>%</c:when><c:otherwise>-</c:otherwise></c:choose></div>
      <div class="label">及格率</div>
    </div>
    <div class="mini-stat">
      <div class="value"><c:choose><c:when test="${not empty report.classStat.excellentRate}"><fmt:formatNumber value="${report.classStat.excellentRate}" maxFractionDigits="1" minFractionDigits="1"/>%</c:when><c:otherwise>-</c:otherwise></c:choose></div>
      <div class="label">优秀率（≥90）</div>
    </div>
    <div class="mini-stat">
      <div class="value">${report.classStat.scoreCount}</div>
      <div class="label">成绩记录</div>
    </div>
    <div class="mini-stat">
      <div class="value">${fn:length(report.students)}</div>
      <div class="label">参与统计学生</div>
    </div>
  </div>

  <div class="grid-2">
    <div class="card">
      <div class="card-header"><h3>分课程统计</h3></div>
      <div class="card-body no-pad">
        <div class="table-wrap">
          <table class="table">
            <thead><tr><th>课程</th><th class="num">成绩数</th><th class="num">平均分</th><th class="num">最高</th><th class="num">最低</th><th>及格率</th></tr></thead>
            <tbody>
            <c:forEach var="co" items="${report.courseStats}">
              <tr>
                <td class="strong"><c:out value="${co.courseName}"/></td>
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
      <div class="card-header"><h3>成绩分布</h3></div>
      <div class="card-body">
        <c:forEach var="d" items="${report.distribution}">
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
    <div class="card-header"><h3>学生成绩汇总与排名</h3><p class="sub">按平均分降序排列</p></div>
    <div class="card-body no-pad">
      <div class="table-wrap">
        <table class="table">
          <thead><tr><th style="width:70px">排名</th><th>学号</th><th>姓名</th><th class="num">已录课程</th><th class="num">平均分</th><th class="num">及格率</th><th class="num">已获学分</th><th>等级</th></tr></thead>
          <tbody>
          <c:forEach var="s" items="${report.students}">
            <tr>
              <td><span class="rank-badge<c:if test="${s.rank eq 1}"> rank-1</c:if><c:if test="${s.rank eq 2}"> rank-2</c:if><c:if test="${s.rank eq 3}"> rank-3</c:if>">${s.rank}</span></td>
              <td class="strong"><c:out value="${s.studentNo}"/></td>
              <td><c:out value="${s.name}"/></td>
              <td class="num">${s.scoreCount}</td>
              <td class="num strong"><c:choose><c:when test="${not empty s.averageScore}"><fmt:formatNumber value="${s.averageScore}" maxFractionDigits="1" minFractionDigits="1"/></c:when><c:otherwise>-</c:otherwise></c:choose></td>
              <td class="num"><c:choose><c:when test="${not empty s.passRate}"><fmt:formatNumber value="${s.passRate}" maxFractionDigits="1" minFractionDigits="1"/>%</c:when><c:otherwise>-</c:otherwise></c:choose></td>
              <td class="num"><fmt:formatNumber value="${s.earnedCredits}" maxFractionDigits="1" minFractionDigits="1"/></td>
              <td><span class="badge badge-${s.levelTone}">${s.level}</span></td>
            </tr>
          </c:forEach>
          <c:if test="${empty report.students}">
            <tr><td colspan="8" class="muted" style="text-align:center;padding:40px">该班级暂无学生</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</c:if>

<%@ include file="/WEB-INF/views/common/footer.jspf" %>
