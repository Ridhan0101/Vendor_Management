CREATE TABLE layanan (
    uraian VARCHAR(500) NOT NULL
);


SELECT v.nama_vendor, l.uraian FROM vendors v  + JOIN uraian ON v.id = uraian.vendor_id  + JOIN layanan l ON uraian.layanan_id = l.id;