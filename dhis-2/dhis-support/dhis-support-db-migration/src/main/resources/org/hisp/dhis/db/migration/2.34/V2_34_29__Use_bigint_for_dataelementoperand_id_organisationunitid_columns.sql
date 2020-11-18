-- dataelementoperand table and its foreign key references
alter table dataelementoperand alter column organisationunitid type bigint;

-- datasetgreyedfields table and its foreign key references
alter table datasetgreyedfields alter column datasetid type bigint;
alter table datasetgreyedfields alter column dataelementoperandid type bigint; 