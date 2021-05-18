-- add column organisationunitid to dataelementoperand table
alter table dataelementoperand add column organisationunitid bigint;

--
-- Name: dataelementoperand fk_dataelementoperand_organisationunit; Type: CONSTRAINT; Schema: public; Owner: -
--
alter table dataelementoperand add CONSTRAINT fk_dataelementoperand_organisationunit FOREIGN KEY (organisationunitid)
        REFERENCES public.organisationunit (organisationunitid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION;