
-- update table "approvalvalidation"
alter table approvalvalidation alter column approvalvalidationid type bigint;
alter table approvalvalidation alter column datasetid type bigint;
alter table approvalvalidation alter column approvalvalidationruleid type bigint;
alter table approvalvalidation alter column periodid type bigint;
alter table approvalvalidation alter column organisationunitid type bigint;
alter table approvalvalidation alter column attributeoptioncomboid type bigint;




-- update table "approvalvalidationaudit"
alter table approvalvalidationaudit alter column approvalvalidationauditid type bigint;
alter table approvalvalidationaudit alter column datasetid type bigint;
alter table approvalvalidationaudit alter column approvalvalidationruleid type bigint;
alter table approvalvalidationaudit alter column periodid type bigint;
alter table approvalvalidationaudit alter column organisationunitid type bigint;
alter table approvalvalidationaudit alter column attributeoptioncomboid type bigint;


  
-- update table "approvalvalidationrule"

alter table approvalvalidationrule alter column approvalvalidationruleid type bigint;
alter table approvalvalidationrule alter column lastupdatedby type bigint;
alter table approvalvalidationrule alter column userid type bigint;


-- update table "approvalvalidationruleorganisationunitlevels"

alter table approvalvalidationruleorganisationunitlevels alter column approvalvalidationruleid type bigint;

