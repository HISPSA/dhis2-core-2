package org.hisp.dhis.approvalvalidationrule;

import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.system.deletion.DeletionHandler;
import org.springframework.jdbc.core.JdbcTemplate;

public class ApprovalValidationAuditDeletionHandler
    extends DeletionHandler
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -------------------------------------------------------------------------
    // DeletionHandler implementation
    // -------------------------------------------------------------------------

    @Override
    public String getClassName()
    {
        return ApprovalValidationAudit.class.getSimpleName();
    }
    
    @Override
    public String allowDeleteDataSet( DataSet dataSet )
    {
        String sql = "SELECT COUNT(*) FROM approvalValidationaudit where dataelementid=" + dataSet.getId();
        
        return jdbcTemplate.queryForObject( sql, Integer.class ) == 0 ? null : ERROR;
    }
    
    @Override
    public String allowDeleteApprovalValidationRule( ApprovalValidationRule approvalValidationRule )
    {
        String sql = "SELECT COUNT(*) FROM approvalValidationaudit where approvalvalidationruleid=" + approvalValidationRule.getId();
        
        return jdbcTemplate.queryForObject( sql, Integer.class ) == 0 ? null : ERROR;
    }
    
    @Override
    public String allowDeletePeriod( Period period )
    {
        String sql = "SELECT COUNT(*) FROM approvalValidationaudit where periodid=" + period.getId();
        
        return jdbcTemplate.queryForObject( sql, Integer.class ) == 0 ? null : ERROR;
    }
    
    @Override
    public String allowDeleteOrganisationUnit( OrganisationUnit unit )
    {
        String sql = "SELECT COUNT(*) FROM approvalValidationaudit where organisationunitid=" + unit.getId();
        
        return jdbcTemplate.queryForObject( sql, Integer.class ) == 0 ? null : ERROR;
    }
    
    @Override
    public String allowDeleteCategoryOptionCombo( CategoryOptionCombo optionCombo )
    {
        String sql = "SELECT COUNT(*) FROM datavalueaudit where attributeoptioncomboid=" + optionCombo.getId();
        
        return jdbcTemplate.queryForObject( sql, Integer.class ) == 0 ? null : ERROR;
    }
}
