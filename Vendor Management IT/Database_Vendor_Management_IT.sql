CREATE DATABASE DATA_VENDOR_PT_DI

CREATE TABLE Vendors (
    id SERIAL PRIMARY KEY,
    nama VARCHAR(255),
    hp VARCHAR(20),
    kota VARCHAR(100),
    status VARCHAR(50)
);

CREATE TABLE kategori (
    Kategori VARCHAR(255),
    keterangan VARCHAR(255)
);
CREATE TABLE uraian (
    layanan_id SERIAL PRIMARY KEY,
    uraian VARCHAR(255)
);

CREATE TABLE analisa (
    id SERIAL PRIMARY KEY,
    nama_vendor VARCHAR(255),
    kategori VARCHAR(255),
    layanan VARCHAR(255)
);

CREATE TABLE sla (
    id SERIAL PRIMARY KEY,
    vendor VARCHAR(255),
    sla_details VARCHAR(255)
);
CREATE TABLE your_table_name (
    vendor VARCHAR(255),
    data_kinerja TEXT,
    rating VARCHAR(255)
);

