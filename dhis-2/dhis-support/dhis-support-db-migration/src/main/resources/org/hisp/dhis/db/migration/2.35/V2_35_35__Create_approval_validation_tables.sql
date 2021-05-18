
-- Create table "approvalvalidationrule"

CREATE TABLE public.approvalvalidationrule
(
    approvalvalidationruleid bigint NOT NULL,
    uid character varying(11) COLLATE pg_catalog."default" NOT NULL,
    code character varying(50) COLLATE pg_catalog."default",
    created timestamp without time zone NOT NULL,
    lastupdated timestamp without time zone NOT NULL,
    lastupdatedby bigint,
    name character varying(230) COLLATE pg_catalog."default" NOT NULL,
    description text COLLATE pg_catalog."default",
    userid bigint,
    skipapprovalvalidation boolean,
    CONSTRAINT approvalvalidationrule_pkey PRIMARY KEY (approvalvalidationruleid),
    CONSTRAINT uk_6ucdre9p2qebt7xeph5xbftq1 UNIQUE (code),
    CONSTRAINT uk_amwxp3aer7tfa6y994m65wghb UNIQUE (name),
    CONSTRAINT uk_dea8islco3yl2w1kjc4eis92i UNIQUE (uid),
    CONSTRAINT fk_lastupdateby_userid FOREIGN KEY (lastupdatedby)
        REFERENCES public.userinfo (userinfoid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_validationrule_userid FOREIGN KEY (userid)
        REFERENCES public.userinfo (userinfoid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Create table "approvalvalidationruleorganisationunitlevels"

CREATE TABLE public.approvalvalidationruleorganisationunitlevels
(
    approvalvalidationruleid bigint NOT NULL,
    organisationunitlevel integer,
    CONSTRAINT fk_organisationunitlevel_approvalvalidationruleid FOREIGN KEY (approvalvalidationruleid)
        REFERENCES public.approvalvalidationrule (approvalvalidationruleid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Create table "approvalvalidation"

CREATE TABLE public.approvalvalidation
(
    approvalvalidationid bigint NOT NULL,
    created timestamp without time zone NOT NULL,
    datasetid bigint,
    approvalvalidationruleid bigint,
    periodid bigint,
    organisationunitid bigint,
    attributeoptioncomboid bigint,
    storedby character varying(255) COLLATE pg_catalog."default",
    notificationsent boolean,
    CONSTRAINT approvalvalidation_pkey PRIMARY KEY (approvalvalidationid),
    CONSTRAINT uk_50yxxoa9gvky6qowicl1ai6y7 UNIQUE (datasetid, approvalvalidationruleid, periodid, organisationunitid, attributeoptioncomboid),
    CONSTRAINT fk_approvalvalidation_approvalvalidationruleid FOREIGN KEY (approvalvalidationruleid)
        REFERENCES public.approvalvalidationrule (approvalvalidationruleid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_approvalvalidation_attributeoptioncomboid FOREIGN KEY (attributeoptioncomboid)
        REFERENCES public.categoryoptioncombo (categoryoptioncomboid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_approvalvalidation_datasetid FOREIGN KEY (datasetid)
        REFERENCES public.dataset (datasetid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_approvalvalidation_organisationunitid FOREIGN KEY (organisationunitid)
        REFERENCES public.organisationunit (organisationunitid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_approvalvalidation_period FOREIGN KEY (periodid)
        REFERENCES public.period (periodid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Create table "approvalvalidationaudit"

CREATE TABLE public.approvalvalidationaudit
(
    approvalvalidationauditid bigint NOT NULL,
    datasetid bigint NOT NULL,
    approvalvalidationruleid bigint NOT NULL,
    periodid bigint NOT NULL,
    organisationunitid bigint NOT NULL,
    attributeoptioncomboid bigint NOT NULL,
    created timestamp without time zone,
    modifiedby character varying(255) COLLATE pg_catalog."default",
    audittype character varying(100) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT approvalvalidationaudit_pkey PRIMARY KEY (approvalvalidationauditid),
    CONSTRAINT fk_approvalvalidation_approvalvalidationruleid FOREIGN KEY (approvalvalidationruleid)
        REFERENCES public.approvalvalidationrule (approvalvalidationruleid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_approvalvalidationaudit_attributeoptioncomboid FOREIGN KEY (attributeoptioncomboid)
        REFERENCES public.categoryoptioncombo (categoryoptioncomboid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_approvalvalidationaudit_datasetid FOREIGN KEY (datasetid)
        REFERENCES public.dataset (datasetid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_approvalvalidationaudit_organisationunitid FOREIGN KEY (organisationunitid)
        REFERENCES public.organisationunit (organisationunitid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_approvalvalidationaudit_periodid FOREIGN KEY (periodid)
        REFERENCES public.period (periodid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

-- Creates index for approvalvalidationauditid for approvalvalidationaudit table

CREATE INDEX id_approvalvalidationaudit_created
    ON public.approvalvalidationaudit USING btree(created ASC NULLS LAST)
    TABLESPACE pg_default;
