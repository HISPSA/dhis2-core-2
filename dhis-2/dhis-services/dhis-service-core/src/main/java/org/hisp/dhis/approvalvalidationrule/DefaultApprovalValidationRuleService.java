package org.hisp.dhis.approvalvalidationrule;

import java.util.List;

import org.hisp.dhis.approvalvalidationrule.ApprovalValidationRule;
import org.hisp.dhis.approvalvalidationrule.ApprovalValidationRuleService;
import org.hisp.dhis.approvalvalidationrule.ApprovalValidationRuleStore;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mike Nelushi
 */
@Transactional
public class DefaultApprovalValidationRuleService
    implements ApprovalValidationRuleService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ApprovalValidationRuleStore approvalValidationRuleStore;

    public void setApprovalValidationRuleStore(ApprovalValidationRuleStore approvalValidationRuleStore) {
		this.approvalValidationRuleStore = approvalValidationRuleStore;
	}


    // -------------------------------------------------------------------------
    // ApprovalValidationRule CRUD operations
    // -------------------------------------------------------------------------

    @Override
    public int saveApprovalValidationRule( ApprovalValidationRule approvalValidationRule )
    {
    	approvalValidationRuleStore.save( approvalValidationRule );

        return approvalValidationRule.getId();
    }

    @Override
    public void updateApprovalValidationRule( ApprovalValidationRule approvalValidationRule )
    {
    	approvalValidationRuleStore.update( approvalValidationRule );
    }

    @Override
    public void deleteApprovalValidationRule( ApprovalValidationRule approvalValidationRule )
    {
    	approvalValidationRuleStore.delete( approvalValidationRule );
    }

    @Override
    public ApprovalValidationRule getApprovalValidationRule( int id )
    {
        return approvalValidationRuleStore.get( id );
    }

    @Override
    public ApprovalValidationRule getApprovalValidationRule( String uid )
    {
        return approvalValidationRuleStore.getByUid( uid );
    }
    
    @Override
    public List<ApprovalValidationRule> getAllApprovalValidationRules()
    {
        return approvalValidationRuleStore.getAllApprovalValidationRules();
    }


    @Override
    public ApprovalValidationRule getApprovalValidationRuleByName( String name )
    {
        return approvalValidationRuleStore.getByName( name );
    }

    

    
}
