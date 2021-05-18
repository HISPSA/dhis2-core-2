
-- Create table "datasetgreyedfields"

CREATE TABLE public.datasetgreyedfields
(
    datasetid bigint NOT NULL,
    dataelementoperandid bigint NOT NULL,
    CONSTRAINT datasetgreyedfields_pkey PRIMARY KEY (datasetid, dataelementoperandid),
    CONSTRAINT fk_dataset_dataelementoperandid FOREIGN KEY (dataelementoperandid)
        REFERENCES public.dataelementoperand (dataelementoperandid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_datasetgreyedfields_datasetid FOREIGN KEY (datasetid)
        REFERENCES public.dataset (datasetid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);
