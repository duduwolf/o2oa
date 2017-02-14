package com.x.attendance.assemble.control.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.attendance.assemble.control.AbstractFactory;
import com.x.attendance.assemble.control.Business;
import com.x.attendance.assemble.control.jaxrs.selfholiday.WrapInFilter;
import com.x.attendance.entity.AttendanceSelfHoliday;
import com.x.attendance.entity.AttendanceSelfHoliday_;
import com.x.base.core.exception.ExceptionWhen;
import com.x.base.core.utils.annotation.MethodDescribe;
/**
 * 系统配置信息表基础功能服务类
 * @author liyi
 */
public class AttendanceSelfHolidayFactory extends AbstractFactory {

	private Logger logger = LoggerFactory.getLogger( AttendanceSelfHolidayFactory.class );
	
	public AttendanceSelfHolidayFactory(Business business) throws Exception {
		super(business);
	}

	@MethodDescribe("获取指定Id的AttendanceSelfHoliday信息对象")
	public AttendanceSelfHoliday get( String id ) throws Exception {
		return this.entityManagerContainer().find(id, AttendanceSelfHoliday.class, ExceptionWhen.none);
	}
	
	@MethodDescribe("列示全部的AttendanceSelfHoliday信息列表")
	public List<String> listAll() throws Exception {
		EntityManager em = this.entityManagerContainer().get(AttendanceSelfHoliday.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<AttendanceSelfHoliday> root = cq.from( AttendanceSelfHoliday.class);
		cq.select(root.get(AttendanceSelfHoliday_.id));
		return em.createQuery(cq).getResultList();
	}
	
	@MethodDescribe("列示指定Id的AttendanceSelfHoliday信息列表")
	public List<AttendanceSelfHoliday> list(List<String> ids) throws Exception {
		if( ids == null || ids.size() == 0 ){
			return new ArrayList<AttendanceSelfHoliday>();
		}
		EntityManager em = this.entityManagerContainer().get(AttendanceSelfHoliday.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<AttendanceSelfHoliday> cq = cb.createQuery(AttendanceSelfHoliday.class);
		Root<AttendanceSelfHoliday> root = cq.from(AttendanceSelfHoliday.class);
		Predicate p = root.get(AttendanceSelfHoliday_.id).in(ids);
		return em.createQuery(cq.where(p)).getResultList();
	}

	@MethodDescribe("列示单个员工的AttendanceSelfHoliday信息列表")
	public List<String> getByEmployeeName(String empName) throws Exception {
		EntityManager em = this.entityManagerContainer().get(AttendanceSelfHoliday.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<AttendanceSelfHoliday> root = cq.from( AttendanceSelfHoliday.class);
		Predicate p = root.get(AttendanceSelfHoliday_.employeeName).in( empName );
		cq.select(root.get(AttendanceSelfHoliday_.id));
		return em.createQuery(cq.where(p)).getResultList();
	}
	
	@MethodDescribe("根据流程的文档ID列示员工的AttendanceSelfHoliday信息列表")
	public List<String> getByWorkFlowDocId(String docId) throws Exception {
		EntityManager em = this.entityManagerContainer().get(AttendanceSelfHoliday.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<AttendanceSelfHoliday> root = cq.from( AttendanceSelfHoliday.class);
		Predicate p = cb.equal(root.get(AttendanceSelfHoliday_.docId), docId);
		cq.select(root.get(AttendanceSelfHoliday_.id));
		return em.createQuery(cq.where(p)).getResultList();
	}

	public List<String> listByStartDateAndEndDate(Date startDate, Date endDate) throws Exception {
		EntityManager em = this.entityManagerContainer().get(AttendanceSelfHoliday.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<AttendanceSelfHoliday> root = cq.from( AttendanceSelfHoliday.class);
		Predicate p = cb.between(root.get(AttendanceSelfHoliday_.startTime), startDate, endDate);
		p = cb.and( p, cb.between(root.get(AttendanceSelfHoliday_.endTime), startDate, endDate));
		cq.select(root.get(AttendanceSelfHoliday_.id));
		return em.createQuery(cq.where(p)).getResultList();
	}
	
	/**
	 * 查询下一页的信息数据
	 * @param id
	 * @param count
	 * @param sequence
	 * @param wrapIn
	 * @return
	 * @throws Exception
	 */
	public List<AttendanceSelfHoliday> listIdsNextWithFilter( String id, Integer count, Object sequence, WrapInFilter wrapIn ) throws Exception {
		//先获取上一页最后一条的sequence值，如果有值的话，以此sequence值作为依据取后续的count条数据
		EntityManager em = this.entityManagerContainer().get( AttendanceSelfHoliday.class );
		String order = wrapIn.getOrder();//排序方式
		List<Object> vs = new ArrayList<>();
		StringBuffer sql_stringBuffer = new StringBuffer();
		
		if( order == null || order.isEmpty() ){
			order = "DESC";
		}
		
		Integer index = 1;
		sql_stringBuffer.append( "SELECT o FROM "+AttendanceSelfHoliday.class.getCanonicalName()+" o where 1=1" );

		if ((null != sequence) ) {
			sql_stringBuffer.append(" and o.sequence " + (StringUtils.equalsIgnoreCase(order, "DESC") ? "<" : ">") + (" ?" + (index)));
			vs.add(sequence);
			index++;
		}
		if ((null != wrapIn.getQ_empName()) && (!wrapIn.getQ_empName().isEmpty())) {
			sql_stringBuffer.append(" and o.employeeName = ?" + (index));
			vs.add( wrapIn.getQ_empName() );
			index++;
		}
		if (null != wrapIn.getDepartmentNames() && wrapIn.getDepartmentNames().size()>0) {
			sql_stringBuffer.append(" and o.organizationName in ( ?" + (index) + ")");
			vs.add( wrapIn.getDepartmentNames() );
			index++;
		}
		if (null != wrapIn.getCompanyNames() && wrapIn.getCompanyNames().size() > 0 ) {
			sql_stringBuffer.append(" and o.companyName in ( ?" + (index) + ")");
			vs.add( wrapIn.getCompanyNames() );
			index++;
		}
		
		if( wrapIn.getKey() != null && !wrapIn.getKey().isEmpty()){
			sql_stringBuffer.append(" order by o."+wrapIn.getKey()+" " + order );
		}else{
			sql_stringBuffer.append(" order by o.sequence " + order );
		}
		
		//logger.debug("listIdsNextWithFilter:["+sql_stringBuffer.toString()+"]");
		//logger.debug( vs );
		
		Query query = em.createQuery( sql_stringBuffer.toString(), AttendanceSelfHoliday.class );
		//为查询设置所有的参数值
		for (int i = 0; i < vs.size(); i++) {
			query.setParameter(i + 1, vs.get(i));
		}
		return query.setMaxResults(count).getResultList();
	}	
	
	/**
	 * 查询上一页的文档信息数据
	 * @param id
	 * @param count
	 * @param sequence
	 * @param wrapIn
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<AttendanceSelfHoliday> listIdsPrevWithFilter( String id, Integer count, Object sequence, WrapInFilter wrapIn ) throws Exception {
		//先获取上一页最后一条的sequence值，如果有值的话，以此sequence值作为依据取后续的count条数据
		EntityManager em = this.entityManagerContainer().get( AttendanceSelfHoliday.class );
		String order = wrapIn.getOrder();//排序方式
		List<Object> vs = new ArrayList<>();
		StringBuffer sql_stringBuffer = new StringBuffer();
		Integer index = 1;
		
		if( order == null || order.isEmpty() ){
			order = "DESC";
		}
		
		sql_stringBuffer.append( "SELECT o FROM "+AttendanceSelfHoliday.class.getCanonicalName()+" o where 1=1" );
		if ((null != sequence) ) {
			sql_stringBuffer.append(" and o.sequence " + (StringUtils.equalsIgnoreCase(order, "DESC") ? ">" : "<") + (" ?" + (index)));
			vs.add(sequence);
			index++;
		}
		if ((null != wrapIn.getQ_empName()) && (!wrapIn.getQ_empName().isEmpty())) {
			sql_stringBuffer.append(" and o.employeeName = ?" + (index));
			vs.add( wrapIn.getQ_empName() );
			index++;
		}
		if (null != wrapIn.getDepartmentNames() && wrapIn.getDepartmentNames().size()>0) {
			sql_stringBuffer.append(" and o.organizationName in ( ?" + (index) + ")");
			vs.add( wrapIn.getDepartmentNames() );
			index++;
		}
		if (null != wrapIn.getCompanyNames() && wrapIn.getCompanyNames().size() > 0 ) {
			sql_stringBuffer.append(" and o.companyName in ( ?" + (index) + ")");
			vs.add( wrapIn.getCompanyNames() );
			index++;
		}
		
		if( wrapIn.getKey() != null && !wrapIn.getKey().isEmpty()){
			sql_stringBuffer.append(" order by o."+wrapIn.getKey()+" " + order );
		}else{
			sql_stringBuffer.append(" order by o.sequence " + order );
		}
		
		//logger.debug("listIdsPrevWithFilter:["+sql_stringBuffer.toString()+"]");
		//logger.debug( vs );
		
		Query query = em.createQuery( sql_stringBuffer.toString(), AttendanceSelfHoliday.class );
		//为查询设置所有的参数值
		for (int i = 0; i < vs.size(); i++) {
			query.setParameter(i + 1, vs.get(i));
		}
		
		return query.setMaxResults(20).getResultList();
	}
	
	/**
	 * 查询符合的文档信息总数
	 * @param id
	 * @param count
	 * @param sequence
	 * @param wrapIn
	 * @return
	 * @throws Exception
	 */
	public long getCountWithFilter( WrapInFilter wrapIn ) throws Exception {
		//先获取上一页最后一条的sequence值，如果有值的话，以此sequence值作为依据取后续的count条数据
		EntityManager em = this.entityManagerContainer().get( AttendanceSelfHoliday.class );
		List<Object> vs = new ArrayList<>();
		StringBuffer sql_stringBuffer = new StringBuffer();
		Integer index = 1;
		
		sql_stringBuffer.append( "SELECT count(o.id) FROM "+AttendanceSelfHoliday.class.getCanonicalName()+" o where 1=1" );
		
		if ((null != wrapIn.getQ_empName()) && (!wrapIn.getQ_empName().isEmpty())) {
			sql_stringBuffer.append(" and o.employeeName = ?" + (index));
			vs.add( wrapIn.getQ_empName() );
			index++;
		}
		if (null != wrapIn.getDepartmentNames() && wrapIn.getDepartmentNames().size()>0) {
			sql_stringBuffer.append(" and o.organizationName in ( ?" + (index) + ")");
			vs.add( wrapIn.getDepartmentNames() );
			index++;
		}
		if (null != wrapIn.getCompanyNames() && wrapIn.getCompanyNames().size() > 0 ) {
			sql_stringBuffer.append(" and o.companyName in ( ?" + (index) + ")");
			vs.add( wrapIn.getCompanyNames() );
			index++;
		}
		
		//logger.debug("listIdsPrevWithFilter:["+sql_stringBuffer.toString()+"]");
		//logger.debug( vs );
		
		Query query = em.createQuery( sql_stringBuffer.toString(), AttendanceSelfHoliday.class );
		//为查询设置所有的参数值
		for (int i = 0; i < vs.size(); i++) {
			query.setParameter(i + 1, vs.get(i));
		}		
		return (Long) query.getSingleResult();
	}
}