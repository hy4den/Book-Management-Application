package com.ornek.kitapyonetimuygulamasi;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ToggleButton;
import android.content.SharedPreferences;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
      private BookManager bookManager;
      private ListView listViewKitaplar;
      private KitapAdapter adapter;
      private List<Book> kitapListesi;
      private EditText editTextArama;
      private List<Book> tumKitaplar;
      private TextView textViewBosListe;
      private ToggleButton toggleFavoriler;
      private boolean sadeceFavoriler = false;
      private static final String PREF_MOCK_VERI = "mock_veri_yuklendi";

      @Override
      protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            bookManager = new BookManager(this);
            listViewKitaplar = findViewById(R.id.listViewKitaplar);
            kitapListesi = new ArrayList<>();
            adapter = new KitapAdapter(this, kitapListesi);
            listViewKitaplar.setAdapter(adapter);

            Button btnEkle = findViewById(R.id.btnEkle);
            Button btnGuncelle = findViewById(R.id.btnGuncelle);
            Button btnSorgula = findViewById(R.id.btnSorgula);
            Button btnSil = findViewById(R.id.btnSil);

            btnEkle.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        kitapEkleDialog();
                  }
            });

            btnGuncelle.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        kitapGuncelleDialog();
                  }
            });

            btnSorgula.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        kitapSorgulaDialog();
                  }
            });

            btnSil.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        kitapSilDialog();
                  }
            });

            editTextArama = findViewById(R.id.editTextArama);
            tumKitaplar = new ArrayList<>();
            editTextArama.addTextChangedListener(new TextWatcher() {
                  @Override
                  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                  }

                  @Override
                  public void onTextChanged(CharSequence s, int start, int before, int count) {
                        kitaplariFiltreleVeGoster(s.toString());
                  }

                  @Override
                  public void afterTextChanged(Editable s) {
                  }
            });

            textViewBosListe = findViewById(R.id.textViewBosListe);

            toggleFavoriler = findViewById(R.id.toggleFavoriler);
            toggleFavoriler.setOnCheckedChangeListener((buttonView, isChecked) -> {
                  sadeceFavoriler = isChecked;
                  kitaplariListeleVeGoster();
            });

            // Mock verileri yükle
            mockVerileriYukle();

            kitaplariListeleVeGoster();

            // Toplam ve favori kitap sayısını göster
            TextView textViewToplamKitap = findViewById(R.id.textViewToplamKitap);
            TextView textViewFavoriKitap = findViewById(R.id.textViewFavoriKitap);
            textViewToplamKitap.setText(getString(R.string.toplam_kitap, bookManager.toplamKitapSayisi()));
            textViewFavoriKitap.setText(getString(R.string.favori_kitap, bookManager.favoriKitapSayisi()));

            listViewKitaplar.setOnItemLongClickListener((parent, view, position, id) -> {
                  if (position < 0 || position >= kitapListesi.size())
                        return false;
                  Book seciliKitap = kitapListesi.get(position);
                  // Ödünçteki kitaplar için seçenekleri düzenle
                  boolean oduncVerildi = bookManager.kitapOduncVerildiMi(seciliKitap.getId());
                  List<String> secenekListesi = new ArrayList<>();
                  secenekListesi.add("Güncelle");
                  secenekListesi.add("Sil");
                  secenekListesi.add("Favori Durumunu Değiştir");
                  secenekListesi.add("Yorumları Göster");
                  if (oduncVerildi) {
                        secenekListesi.add("Ödünç Bilgilerini Göster");
                  } else {
                        secenekListesi.add("Ödünç Ver");
                  }
                  String[] secenekler = secenekListesi.toArray(new String[0]);

                  new AlertDialog.Builder(this)
                              .setTitle("İşlem Seçin")
                              .setItems(secenekler, (dialog, which) -> {
                                    String secilenIslem = secenekler[which];
                                    if (secilenIslem.equals("Güncelle")) {
                                          // Güncelle
                                          kitapGuncelleDialogHazirla(seciliKitap);
                                    } else if (secilenIslem.equals("Sil")) {
                                          // Sil
                                          new AlertDialog.Builder(this)
                                                      .setTitle("Onay")
                                                      .setMessage("Bu kitabı silmek istediğinize emin misiniz?")
                                                      .setPositiveButton("Evet", (d, w) -> {
                                                            bookManager.kitapSil(seciliKitap.getId());
                                                            kitaplariListeleVeGoster();
                                                            Toast.makeText(this, "Kitap silindi!", Toast.LENGTH_SHORT)
                                                                        .show();
                                                      })
                                                      .setNegativeButton("Hayır", null)
                                                      .show();
                                    } else if (secilenIslem.equals("Favori Durumunu Değiştir")) {
                                          // Favori durumunu değiştir
                                          seciliKitap.setFavori(!seciliKitap.isFavori());
                                          bookManager.kitapGuncelle(seciliKitap);
                                          kitaplariListeleVeGoster(); // Liste ve adaptör güncellenmeli
                                          String mesaj = seciliKitap.isFavori() ? "Favorilere eklendi!"
                                                      : "Favorilerden çıkarıldı!";
                                          Toast.makeText(this, mesaj, Toast.LENGTH_SHORT).show();
                                    } else if (secilenIslem.equals("Yorumları Göster")) {
                                          // Yorumları Göster
                                          yorumlariGosterDialog(seciliKitap);
                                    } else if (secilenIslem.equals("Ödünç Ver")) {
                                          // Ödünç Ver
                                          oduncVerDialog(seciliKitap);
                                    } else if (secilenIslem.equals("Ödünç Bilgilerini Göster")) {
                                          // Ödünç Bilgilerini Göster
                                          oduncBilgileriniGosterDialog(seciliKitap);
                                    }
                              })
                              .show();
                  return true;
            });
      }

      /**
       * Mock verileri bir kez yükler
       */
      private void mockVerileriYukle() {
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            boolean mockVeriYuklendi = prefs.getBoolean(PREF_MOCK_VERI, false);

            if (!mockVeriYuklendi) {
                  // Örnek kitapları ekleme ve ID'lerini alma
                  long[] kitapIdleri = new long[10];
                  kitapIdleri[0] = bookManager.kitapEkle(new Book(0, "Suç ve Ceza", 1866, "Fyodor Dostoyevski",
                                          "Psikolojik bir roman. Raskolnikov isimli bir üniversite öğrencisinin hikayesi.",
                                          true, new Category(0, "Roman")));
                  kitapIdleri[1] = bookManager.kitapEkle(new Book(0, "Sefiller", 1862, "Victor Hugo",
                                          "Fransız toplumunda adaletsizlik ve yoksulluk temalı epik roman.", true, new Category(0, "Roman")));
                  kitapIdleri[2] = bookManager.kitapEkle(new Book(0, "1984", 1949, "George Orwell", "Distopik bir gelecekte geçen politik roman.",
                                          false, new Category(0, "Bilim")));
                  kitapIdleri[3] = bookManager.kitapEkle(new Book(0, "Yüzüklerin Efendisi", 1954, "J.R.R. Tolkien",
                                          "Orta Dünya'da geçen epik fantastik roman serisi.", true, new Category(0, "Fantastik")));
                  kitapIdleri[4] = bookManager.kitapEkle(new Book(0, "Simyacı", 1988, "Paulo Coelho",
                                          "Santiago'nun kişisel efsanesini aramak için yaptığı yolculuk.", false, new Category(0, "Roman")));
                  kitapIdleri[5] = bookManager.kitapEkle(new Book(0, "Dönüşüm", 1915, "Franz Kafka",
                                          "Gregor Samsa'nın bir böceğe dönüşmesi ile başlayan absürt hikaye.", true, new Category(0, "Roman")));
                  kitapIdleri[6] = bookManager.kitapEkle(new Book(0, "Savaş ve Barış", 1869, "Lev Tolstoy",
                                          "Napolyon'un Rusya işgali sırasında beş aristokrat ailenin hikayesi.", false, new Category(0, "Tarih")));
                  kitapIdleri[7] = bookManager.kitapEkle(new Book(0, "Bülbülü Öldürmek", 1960, "Harper Lee",
                                          "Irk ayrımcılığı ve adaletsizlik üzerine güçlü bir roman.", true, new Category(0, "Roman")));
                  kitapIdleri[8] = bookManager.kitapEkle(new Book(0, "Don Kişot", 1605, "Miguel de Cervantes",
                                          "Modern romanın atası sayılan, şövalyelik hikayelerinin parodisi.", false, new Category(0, "Roman")));
                  kitapIdleri[9] = bookManager.kitapEkle(new Book(0, "Küçük Prens", 1943, "Antoine de Saint-Exupéry",
                                          "Çocuklar için yazılmış ama yetişkinlere hitap eden felsefi bir masal.", true, new Category(0, "Çocuk")));

                  // --- Loan (ödünç) mock verileri ---
                  // Ödünç verilecek kitapların ID'lerini doğru şekilde kullan
                  if (kitapIdleri[0] > 0) {
                        bookManager.oduncVer(new Loan(0, (int) kitapIdleri[0], "Ali Veli", new java.util.Date(), null, false));
                  }
                  if (kitapIdleri[2] > 0) {
                        // İkinci ödünç için iade tarihi de ekleyelim
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTime(new java.util.Date());
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -5); // 5 gün önce ödünç verilmiş olsun
                        java.util.Date oduncTarihi = cal.getTime();
                        cal.add(java.util.Calendar.DAY_OF_YEAR, 10); // 5 gün sonra iade edilecek olsun (veya edildi)
                        java.util.Date iadeTarihi = cal.getTime();
                        bookManager.oduncVer(new Loan(0, (int) kitapIdleri[2], "Ayşe Yılmaz", oduncTarihi, iadeTarihi, true)); // iade edildi olarak işaretle
                  }
                  if (kitapIdleri[4] > 0) { // Bir kitap daha ödünçte olsun, iade edilmemiş
                        bookManager.oduncVer(new Loan(0, (int) kitapIdleri[4], "Fatma Kaya", new java.util.Date(), null, false));
                  }

                  // --- Review (yorum) mock verileri ---
                  // Yorum yapılacak kitapların ID'lerini doğru şekilde kullan
                  if (kitapIdleri[0] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[0], "Zeynep", "Harika bir roman! Kesinlikle okunmalı.", 5));
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[0], "Ahmet", "Beklediğimden daha iyiydi.", 4));
                  }
                  if (kitapIdleri[1] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[1], "Mehmet", "Çok sürükleyici ve etkileyici bir anlatım.", 4));
                  }
                  if (kitapIdleri[2] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[2], "Elif", "Düşündürücü ve unutulmaz bir eser.", 5));
                  }
                  if (kitapIdleri[3] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[3], "Can", "Fantastik severler için başyapıt.", 5));
                  }
                  if (kitapIdleri[4] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[4], "Deniz", "Kısa ama etkili bir kitap.", 4));
                  }
                  if (kitapIdleri[5] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[5], "Ali", "Absürt ve düşündürücü bir eser.", 5));
                  }
                  if (kitapIdleri[6] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[6], "Veli", "Tarihsel olayları çok iyi yansıtmış.", 4));
                  }
                  if (kitapIdleri[7] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[7], "Ayşe", "Toplumsal sorunlara dikkat çeken bir roman.", 5));
                  }
                  if (kitapIdleri[8] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[8], "Fatma", "Klasik bir eser, herkes okumalı.", 5));
                  }
                  if (kitapIdleri[9] > 0) {
                        bookManager.yorumEkle(new Review(0, (int) kitapIdleri[9], "Emre", "Çocuklar için güzel bir masal.", 4));
                  }

                  // Mock verilerin yüklendiğini kaydet
                  SharedPreferences.Editor editor = prefs.edit();
                  editor.putBoolean(PREF_MOCK_VERI, true);
                  editor.apply();

                  // Kullanıcıya bildir
                  Toast.makeText(this, "Mock veriler yüklendi!", Toast.LENGTH_SHORT).show();
            }
      }

      private void kitaplariListeleVeGoster() {
            kitapListesi.clear();
            tumKitaplar = bookManager.kitaplariListele();
            for (Book book : tumKitaplar) {
                  if (!sadeceFavoriler || book.isFavori()) {
                        kitapListesi.add(book);
                  }
            }
            adapter.notifyDataSetChanged();
            if (kitapListesi.isEmpty()) {
                  textViewBosListe.setVisibility(View.VISIBLE);
            } else {
                  textViewBosListe.setVisibility(View.GONE);
            }
      }

      private void kitaplariFiltreleVeGoster(String arama) {
            kitapListesi.clear();
            for (Book book : tumKitaplar) {
                  if ((!sadeceFavoriler || book.isFavori()) &&
                              (book.getName().toLowerCase().contains(arama.toLowerCase()) ||
                                          book.getAuthor().toLowerCase().contains(arama.toLowerCase()) ||
                                          book.getAciklama().toLowerCase().contains(arama.toLowerCase()))) {
                        kitapListesi.add(book);
                  }
            }
            adapter.notifyDataSetChanged();
            if (kitapListesi.isEmpty()) {
                  textViewBosListe.setVisibility(View.VISIBLE);
            } else {
                  textViewBosListe.setVisibility(View.GONE);
            }
      }

      private void kitapEkleDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Kitap Ekle");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            final EditText inputAd = new EditText(this);
            inputAd.setHint("Kitap Adı");
            inputAd.setText("");
            layout.addView(inputAd);

            final EditText inputYil = new EditText(this);
            inputYil.setHint("Yayın Yılı");
            inputYil.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            inputYil.setText("");
            layout.addView(inputYil);

            final EditText inputYazar = new EditText(this);
            inputYazar.setHint("Yazar");
            inputYazar.setText("");
            layout.addView(inputYazar);

            final EditText inputAciklama = new EditText(this);
            inputAciklama.setHint("Açıklama");
            inputAciklama.setText("");
            layout.addView(inputAciklama);

            // Kategori Spinner'ı ekle
            final android.widget.Spinner spinnerKategori = new android.widget.Spinner(this);
            java.util.ArrayList<String> kategoriler = new java.util.ArrayList<>();
            kategoriler.add("Roman");
            kategoriler.add("Bilim");
            kategoriler.add("Tarih");
            kategoriler.add("Çocuk");
            kategoriler.add("Diğer");
            android.widget.ArrayAdapter<String> kategoriAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategoriler);
            kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerKategori.setAdapter(kategoriAdapter);
            layout.addView(spinnerKategori);

            final CheckBox checkBoxFavori = new CheckBox(this);
            checkBoxFavori.setText("Favori kitap");
            checkBoxFavori.setChecked(false);
            layout.addView(checkBoxFavori);

            builder.setView(layout);

            builder.setPositiveButton("Ekle", (dialog, which) -> {
                  String ad = inputAd.getText().toString().trim();
                  String yilStr = inputYil.getText().toString().trim();
                  String yazar = inputYazar.getText().toString().trim();
                  String aciklama = inputAciklama.getText().toString().trim();
                  boolean favori = checkBoxFavori.isChecked();
                  String kategoriAdi = spinnerKategori.getSelectedItem().toString();
                  Category kategori = new Category(0, kategoriAdi);
                  int yil = 0;
                  try {
                        yil = Integer.parseInt(yilStr);
                  } catch (NumberFormatException e) {
                        Toast.makeText(this, "Yıl sayısal olmalı!", Toast.LENGTH_SHORT).show();
                        return;
                  }
                  Book yeniKitap = new Book(0, ad, yil, yazar, aciklama, favori, kategori);
                  bookManager.kitapEkle(yeniKitap);
                  editTextArama.setText("");
                  kitaplariListeleVeGoster();
                  Toast.makeText(this, "Kitap eklendi!", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("İptal", null);
            builder.show();
      }

      private void kitapGuncelleDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Kitap Güncelle");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            final EditText inputId = new EditText(this);
            inputId.setHint("Güncellenecek Kitap ID");
            inputId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            inputId.setText("");
            layout.addView(inputId);

            final EditText inputAd = new EditText(this);
            inputAd.setHint("Yeni Kitap Adı");
            inputAd.setText("");
            layout.addView(inputAd);

            final EditText inputYil = new EditText(this);
            inputYil.setHint("Yeni Yayın Yılı");
            inputYil.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            inputYil.setText("");
            layout.addView(inputYil);

            final EditText inputYazar = new EditText(this);
            inputYazar.setHint("Yeni Yazar");
            inputYazar.setText("");
            layout.addView(inputYazar);

            final EditText inputAciklama = new EditText(this);
            inputAciklama.setHint("Yeni Açıklama");
            inputAciklama.setText("");
            layout.addView(inputAciklama);

            // Kategori Spinner'ı ekle
            final android.widget.Spinner spinnerKategori = new android.widget.Spinner(this);
            java.util.ArrayList<String> kategoriler = new java.util.ArrayList<>();
            kategoriler.add("Roman");
            kategoriler.add("Bilim");
            kategoriler.add("Tarih");
            kategoriler.add("Çocuk");
            kategoriler.add("Diğer");
            android.widget.ArrayAdapter<String> kategoriAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategoriler);
            kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerKategori.setAdapter(kategoriAdapter);
            layout.addView(spinnerKategori);

            final CheckBox checkBoxFavori = new CheckBox(this);
            checkBoxFavori.setText("Favori kitap");
            checkBoxFavori.setChecked(false);
            layout.addView(checkBoxFavori);

            builder.setView(layout);

            builder.setPositiveButton("Güncelle", (dialog, which) -> {
                  String idStr = inputId.getText().toString().trim();
                  String ad = inputAd.getText().toString().trim();
                  String yilStr = inputYil.getText().toString().trim();
                  String yazar = inputYazar.getText().toString().trim();
                  String aciklama = inputAciklama.getText().toString().trim();
                  boolean favori = checkBoxFavori.isChecked();
                  String kategoriAdi = spinnerKategori.getSelectedItem().toString();
                  Category kategori = new Category(0, kategoriAdi);

                  if (idStr.isEmpty() || ad.isEmpty() || yilStr.isEmpty() || yazar.isEmpty()) {
                        Toast.makeText(this, "Tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
                        return;
                  }
                  int id, yil;
                  try {
                        id = Integer.parseInt(idStr);
                        yil = Integer.parseInt(yilStr);
                  } catch (NumberFormatException e) {
                        Toast.makeText(this, "ID ve Yıl sayısal olmalı!", Toast.LENGTH_SHORT).show();
                        return;
                  }
                  new AlertDialog.Builder(this)
                              .setTitle("Onay")
                              .setMessage("Bu kitabı güncellemek istediğinize emin misiniz?")
                              .setPositiveButton("Evet", (d, w) -> {
                                    Book guncelKitap = new Book(id, ad, yil, yazar, aciklama, favori, kategori);
                                    bookManager.kitapGuncelle(guncelKitap);
                                    editTextArama.setText("");
                                    kitaplariListeleVeGoster();
                                    // Sayaçları güncelle
                                    TextView textViewToplamKitap = findViewById(R.id.textViewToplamKitap);
                                    TextView textViewFavoriKitap = findViewById(R.id.textViewFavoriKitap);
                                    textViewToplamKitap.setText(getString(R.string.toplam_kitap, bookManager.toplamKitapSayisi()));
                                    textViewFavoriKitap.setText(getString(R.string.favori_kitap, bookManager.favoriKitapSayisi()));
                                    Toast.makeText(this, "Kitap güncellendi!", Toast.LENGTH_SHORT).show();
                              })
                              .setNegativeButton("Hayır", null)
                              .show();
            });
            builder.setNegativeButton("İptal", null);
            builder.show();
      }

      private void kitapSorgulaDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Kitap Sorgula");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            final EditText inputId = new EditText(this);
            inputId.setHint("Sorgulanacak Kitap ID");
            inputId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(inputId);

            builder.setView(layout);

            builder.setPositiveButton("Sorgula", (dialog, which) -> {
                  String idStr = inputId.getText().toString().trim();
                  if (idStr.isEmpty()) {
                        Toast.makeText(this, "Lütfen bir ID girin!", Toast.LENGTH_SHORT).show();
                        return;
                  }
                  int id;
                  try {
                        id = Integer.parseInt(idStr);
                  } catch (NumberFormatException e) {
                        Toast.makeText(this, "Geçerli bir ID girin!", Toast.LENGTH_SHORT).show();
                        return;
                  }
                  Book kitap = bookManager.kitapGetir(id);
                  if (kitap != null) {
                        String bilgi = kitap.getId() + " - " + kitap.getName() + " (" + kitap.getYear() + ")\nYazar: "
                                    + kitap.getAuthor()
                                    + (kitap.getAciklama().isEmpty() ? "" : "\nAçıklama: " + kitap.getAciklama());
                        new AlertDialog.Builder(this)
                                    .setTitle("Kitap Bilgisi")
                                    .setMessage(bilgi)
                                    .setPositiveButton("Tamam", null)
                                    .show();
                  } else {
                        Toast.makeText(this, "Kitap bulunamadı!", Toast.LENGTH_SHORT).show();
                  }
            });
            builder.setNegativeButton("İptal", null);
            builder.show();
      }

      private void kitapSilDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Kitap Sil");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            final EditText inputId = new EditText(this);
            inputId.setHint("Silinecek Kitap ID (boş bırakılırsa tüm kitaplar silinir)");
            inputId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            layout.addView(inputId);

            builder.setView(layout);

            builder.setPositiveButton("Sil", (dialog, which) -> {
                  String idStr = inputId.getText().toString().trim();
                  // Onay diyaloğu
                  new AlertDialog.Builder(this)
                              .setTitle("Onay")
                              .setMessage(idStr.isEmpty() ? "Tüm kitapları silmek istediğinize emin misiniz?"
                                          : "Bu kitabı silmek istediğinize emin misiniz?")
                              .setPositiveButton("Evet", (d, w) -> {
                                    if (idStr.isEmpty()) {
                                          bookManager.tumKitaplariSil();
                                          Toast.makeText(this, "Tüm kitaplar silindi!", Toast.LENGTH_SHORT).show();
                                    } else {
                                          try {
                                                int id = Integer.parseInt(idStr);
                                                bookManager.kitapSil(id);
                                                Toast.makeText(this, "Kitap silindi!", Toast.LENGTH_SHORT).show();
                                          } catch (NumberFormatException e) {
                                                Toast.makeText(this, "Geçerli bir ID girin!", Toast.LENGTH_SHORT)
                                                            .show();
                                          }
                                    }
                                    kitaplariListeleVeGoster();
                              })
                              .setNegativeButton("Hayır", null)
                              .show();
            });
            builder.setNegativeButton("İptal", null);
            builder.show();
      }

      private void kitapGuncelleDialogHazirla(Book kitap) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Kitap Güncelle");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 40, 50, 10);

            final EditText inputId = new EditText(this);
            inputId.setHint("Güncellenecek Kitap ID");
            inputId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            inputId.setText(String.valueOf(kitap.getId()));
            layout.addView(inputId);

            final EditText inputAd = new EditText(this);
            inputAd.setHint("Yeni Kitap Adı");
            inputAd.setText(kitap.getName());
            layout.addView(inputAd);

            final EditText inputYil = new EditText(this);
            inputYil.setHint("Yeni Yayın Yılı");
            inputYil.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            inputYil.setText(String.valueOf(kitap.getYear()));
            layout.addView(inputYil);

            final EditText inputYazar = new EditText(this);
            inputYazar.setHint("Yeni Yazar");
            inputYazar.setText(kitap.getAuthor());
            layout.addView(inputYazar);

            final EditText inputAciklama = new EditText(this);
            inputAciklama.setHint("Yeni Açıklama");
            inputAciklama.setText(kitap.getAciklama());
            layout.addView(inputAciklama);

            // Kategori Spinner'ı ekle
            final android.widget.Spinner spinnerKategori = new android.widget.Spinner(this);
            java.util.ArrayList<String> kategoriler = new java.util.ArrayList<>();
            kategoriler.add("Roman");
            kategoriler.add("Bilim");
            kategoriler.add("Tarih");
            kategoriler.add("Çocuk");
            kategoriler.add("Diğer");
            android.widget.ArrayAdapter<String> kategoriAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kategoriler);
            kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerKategori.setAdapter(kategoriAdapter);
            // Mevcut kategori seçili gelsin
            if (kitap.getCategory() != null) {
                  int idx = kategoriler.indexOf(kitap.getCategory().getName());
                  if (idx >= 0) spinnerKategori.setSelection(idx);
            }
            layout.addView(spinnerKategori);

            final CheckBox checkBoxFavori = new CheckBox(this);
            checkBoxFavori.setText("Favori kitap");
            checkBoxFavori.setChecked(kitap.isFavori());
            layout.addView(checkBoxFavori);

            builder.setView(layout);

            builder.setPositiveButton("Güncelle", (dialog, which) -> {
                  String idStr = inputId.getText().toString().trim();
                  String ad = inputAd.getText().toString().trim();
                  String yilStr = inputYil.getText().toString().trim();
                  String yazar = inputYazar.getText().toString().trim();
                  String aciklama = inputAciklama.getText().toString().trim();
                  boolean favori = checkBoxFavori.isChecked();
                  String kategoriAdi = spinnerKategori.getSelectedItem().toString();
                  Category kategori = new Category(0, kategoriAdi);

                  if (idStr.isEmpty() || ad.isEmpty() || yilStr.isEmpty() || yazar.isEmpty()) {
                        Toast.makeText(this, "Tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
                        return;
                  }
                  int id, yil;
                  try {
                        id = Integer.parseInt(idStr);
                        yil = Integer.parseInt(yilStr);
                  } catch (NumberFormatException e) {
                        Toast.makeText(this, "ID ve Yıl sayısal olmalı!", Toast.LENGTH_SHORT).show();
                        return;
                  }
                  new AlertDialog.Builder(this)
                              .setTitle("Onay")
                              .setMessage("Bu kitabı güncellemek istediğinize emin misiniz?")
                              .setPositiveButton("Evet", (d, w) -> {
                                    Book guncelKitap = new Book(id, ad, yil, yazar, aciklama, favori, kategori);
                                    bookManager.kitapGuncelle(guncelKitap);
                                    editTextArama.setText("");
                                    kitaplariListeleVeGoster();
                                    // Sayaçları güncelle
                                    TextView textViewToplamKitap = findViewById(R.id.textViewToplamKitap);
                                    TextView textViewFavoriKitap = findViewById(R.id.textViewFavoriKitap);
                                    textViewToplamKitap.setText(getString(R.string.toplam_kitap, bookManager.toplamKitapSayisi()));
                                    textViewFavoriKitap.setText(getString(R.string.favori_kitap, bookManager.favoriKitapSayisi()));
                                    Toast.makeText(this, "Kitap güncellendi!", Toast.LENGTH_SHORT).show();
                              })
                              .setNegativeButton("Hayır", null)
                              .show();
            });
            builder.setNegativeButton("İptal", null);
            builder.show();
      }

      // Kitabın yorumlarını gösteren dialog fonksiyonu
      private void yorumlariGosterDialog(Book kitap) {
        List<Review> yorumlar = bookManager.yorumlariGetir(kitap.getId());
        StringBuilder sb = new StringBuilder();
        if (yorumlar == null || yorumlar.isEmpty()) { // Null kontrolü eklendi
            sb.append("Bu kitap için hiç yorum yok.");
        } else {
            for (Review r : yorumlar) {
                sb.append("- ").append(r.getReviewerName()).append(": ")
                  .append(r.getComment()).append(" (Puan: ")
                  .append(r.getRating()).append(")\\n");
            }
        }
        new AlertDialog.Builder(this)
            .setTitle(kitap.getName() + " - Yorumlar")
            .setMessage(sb.toString())
            .setPositiveButton("Kapat", null)
            .show();
    }

    private void oduncVerDialog(final Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        // Ödünç verme dialog layout dosyası olmalı, eğer yoksa basit bir layout oluşturulabilir.
        // Örnek olarak R.layout.dialog_loan_book kullanıldı, bu dosyanın projenizde olduğundan emin olun.
        // Eğer yoksa, manuel olarak EditText içeren bir LinearLayout oluşturabilirsiniz.
        View dialogView = inflater.inflate(R.layout.dialog_odunc_ver, null); // dialog_odunc_ver.xml layout\'unu kullan
        builder.setView(dialogView);

        final EditText editBorrower = dialogView.findViewById(R.id.editTextOduncAlan); // Layout\'taki ID ile eşleşmeli
        // final DatePicker datePicker = dialogView.findViewById(R.id.datePickerOduncTarihi); // Layout\'taki ID ile eşleşmeli

        builder.setTitle("Kitabı Ödünç Ver")
               .setPositiveButton("Ödünç Ver", (dialog, id) -> {
                    String borrowerName = editBorrower.getText().toString().trim();
                    if (borrowerName.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Lütfen ödünç alan kişi adını girin.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // DatePicker\'dan tarih almak yerine şimdiki zamanı ve null bir returnDate kullanalım
                    // Kullanıcı isterse daha sonra iade tarihi ekleyebilir.
                    java.util.Date loanDate = new java.util.Date(); // Şimdiki zaman
                    // java.util.Calendar calendar = java.util.Calendar.getInstance();
                    // calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                    // java.util.Date dueDate = calendar.getTime(); // BudueDate şimdilik kullanılmıyor, null olabilir

                    Loan loan = new Loan(0, book.getId(), borrowerName, loanDate, null, false);
                    bookManager.oduncVer(loan);
                    kitaplariListeleVeGoster(); // Listeyi güncelle
                    Toast.makeText(MainActivity.this, "Kitap ödünç verildi: " + borrowerName, Toast.LENGTH_SHORT).show();
               })
               .setNegativeButton("İptal", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void oduncBilgileriniGosterDialog(Book kitap) {
        List<Loan> oduncKayitlari = bookManager.kitapOduncleriGetir(kitap.getId()); // BookManager\'da bu metod olmalı
        StringBuilder sb = new StringBuilder();

        if (oduncKayitlari == null || oduncKayitlari.isEmpty()) {
            sb.append("Bu kitap için aktif bir ödünç kaydı bulunamadı.");
        } else {
            // Genellikle bir kitabın aynı anda sadece bir kişiye ödünç verildiğini varsayarsak,
            // son ödünç kaydını veya aktif olanı göstermek yeterli olabilir.
            // Eğer birden fazla aktif ödünç kaydı olabiliyorsa, tümünü listelemek gerekir.
            // Şimdilik son (veya tek) aktif ödünç kaydını gösterelim.
            Loan sonOdunc = null;
            for(Loan l : oduncKayitlari){
                if(l.getReturnDate() == null){ // Henüz iade edilmemişse aktiftir
                    sonOdunc = l;
                    break;
                }
            }

            if (sonOdunc != null) {
                sb.append("Ödünç Alan: ").append(sonOdunc.getBorrowerName()).append("\\n");
                sb.append("Ödünç Tarihi: ").append(new java.text.SimpleDateFormat("dd/MM/yyyy").format(sonOdunc.getLoanDate())).append("\\n");
                if (sonOdunc.getReturnDate() != null) {
                    sb.append("İade Tarihi: ").append(new java.text.SimpleDateFormat("dd/MM/yyyy").format(sonOdunc.getReturnDate()));
                } else {
                    sb.append("İade Tarihi: Henüz iade edilmedi.");
                }
            } else {
                 sb.append("Bu kitap için aktif bir ödünç kaydı bulunamadı (tümü iade edilmiş olabilir).");
            }
        }

        new AlertDialog.Builder(this)
            .setTitle(kitap.getName() + " - Ödünç Bilgileri")
            .setMessage(sb.toString())
            .setPositiveButton("Tamam", null)
            // İade etme seçeneği eklenebilir
            // .setNeutralButton("İade Edildi Olarak İşaretle", (dialog, which) -> {
            //    // İade işlemini burada yap
            // })
            .show();
    }

      // Özel Kitap Adaptörü
      private class KitapAdapter extends BaseAdapter {
            private Context context;
            private List<Book> kitaplar;
            private LayoutInflater inflater;

            public KitapAdapter(Context context, List<Book> kitaplar) {
                  this.context = context;
                  this.kitaplar = kitaplar;
                  this.inflater = LayoutInflater.from(context);
            }

            @Override
            public int getCount() {
                  return kitaplar.size();
            }

            @Override
            public Object getItem(int position) {
                  return kitaplar.get(position);
            }

            @Override
            public long getItemId(int position) {
                  return kitaplar.get(position).getId();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                  ViewHolder holder;

                  if (convertView == null) {
                        convertView = inflater.inflate(R.layout.kitap_item, parent, false);
                        holder = new ViewHolder();
                        holder.kitapAd = convertView.findViewById(R.id.textViewKitapAd);
                        holder.yazar = convertView.findViewById(R.id.textViewYazar);
                        holder.yil = convertView.findViewById(R.id.textViewYil);
                        holder.aciklama = convertView.findViewById(R.id.textViewAciklama);
                        holder.favori = convertView.findViewById(R.id.imageViewFavori);
                        holder.kategori = convertView.findViewById(R.id.textViewKategori);
                        // holder.btnOduncVer = convertView.findViewById(R.id.btnOduncVer); // Bu buton artık uzun basma menüsünde
                        holder.btnYorumEkle = convertView.findViewById(R.id.btnYorumEkle);
                        holder.textViewOduncDurumu = convertView.findViewById(R.id.textViewOduncDurumu); // Yeni TextView
                        convertView.setTag(holder);
                  } else {
                        holder = (ViewHolder) convertView.getTag();
                  }

                  Book kitap = kitaplar.get(position);
                  holder.kitapAd.setText(kitap.getId() + " - " + kitap.getName());
                  holder.yazar.setText("Yazar: " + kitap.getAuthor());
                  holder.yil.setText("Yayın Yılı: " + kitap.getYear());

                  if (kitap.getAciklama() != null && !kitap.getAciklama().isEmpty()) {
                        holder.aciklama.setVisibility(View.VISIBLE);
                        holder.aciklama.setText(kitap.getAciklama());
                  } else {
                        holder.aciklama.setVisibility(View.GONE);
                  }

                  // Favori yıldızının her zaman görünür olması ve doğru ikonu göstermesi
                  holder.favori.setVisibility(View.VISIBLE);
                  holder.favori.setImageResource(kitap.isFavori() ? R.drawable.ic_star : R.drawable.ic_star_border);


                  // Ödünç durumu kontrolü ve arayüzde gösterim
                  boolean oduncVerildi = bookManager.kitapOduncVerildiMi(kitap.getId());
                  if (oduncVerildi) {
                      // Ödünçte ise durumu gösteren bir TextView ekleyelim (kitap_item.xml\'e eklenmeli)
                      // Örneğin: <TextView android:id="@+id/textViewOduncDurumu" ... />
                      holder.textViewOduncDurumu.setText("ÖDÜNÇTE");
                      holder.textViewOduncDurumu.setVisibility(View.VISIBLE);
                      // holder.btnOduncVer.setEnabled(false); // Buton kaldırıldığı için bu satıra gerek yok
                      // holder.btnOduncVer.setText("Ödünçte");
                      // holder.btnOduncVer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getResources().getColor(R.color.colorDivider)));
                  } else {
                      holder.textViewOduncDurumu.setVisibility(View.GONE);
                      // holder.btnOduncVer.setEnabled(true);
                      // holder.btnOduncVer.setText("Ödünç Ver");
                      // holder.btnOduncVer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getResources().getColor(R.color.colorAccent)));
                  }

                  if (kitap.getCategory() != null) {
                        holder.kategori.setVisibility(View.VISIBLE);
                        holder.kategori.setText("Kategori: " + kitap.getCategory().getName());
                  } else {
                        holder.kategori.setVisibility(View.GONE);
                  }

                  // Ödünç Ver butonu işlevi (Artık uzun basma menüsünde olduğu için bu kısım kaldırılabilir veya değiştirilebilir)
                  /*
                  holder.btnOduncVer.setOnClickListener(v -> {
                        if (bookManager.kitapOduncVerildiMi(kitap.getId())) {
                              Toast.makeText(context, "Bu kitap zaten ödünçte!", Toast.LENGTH_SHORT).show();
                              // Ödünç bilgilerini gösteren bir dialog açılabilir.
                              oduncBilgileriniGosterDialog(kitap);
                              return;
                        }
                        oduncVerDialog(kitap); // MainActivity\'deki metodu çağır
                  });
                  */

                  // Favori yıldızına tıklayınca favori durumu değişsin
                  holder.favori.setOnClickListener(v -> {
                        kitap.setFavori(!kitap.isFavori());
                        bookManager.kitapGuncelle(kitap);
                        // Liste anında güncellenmeli
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).kitaplariListeleVeGoster();
                        }
                        // Favori sayaçlarını güncelle
                        if (context instanceof MainActivity) {
                              MainActivity act = (MainActivity) context;
                              TextView textViewToplamKitap = act.findViewById(R.id.textViewToplamKitap);
                              TextView textViewFavoriKitap = act.findViewById(R.id.textViewFavoriKitap);
                              if (textViewToplamKitap != null) textViewToplamKitap.setText(act.getString(R.string.toplam_kitap, bookManager.toplamKitapSayisi()));
                              if (textViewFavoriKitap != null) textViewFavoriKitap.setText(act.getString(R.string.favori_kitap, bookManager.favoriKitapSayisi()));
                        }
                        String mesaj = kitap.isFavori() ? "Favorilere eklendi!" : "Favorilerden çıkarıldı!";
                        Toast.makeText(context, mesaj, Toast.LENGTH_SHORT).show();
                  });

                  // Yorum Ekle butonu işlevi
                  holder.btnYorumEkle.setOnClickListener(v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Yorum Ekle");
                        LinearLayout layout = new LinearLayout(context);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(50, 40, 50, 10);
                        final EditText inputAd = new EditText(context);
                        inputAd.setHint("Adınız");
                        layout.addView(inputAd);
                        final EditText inputYorum = new EditText(context);
                        inputYorum.setHint("Yorumunuz");
                        layout.addView(inputYorum);
                        final EditText inputPuan = new EditText(context);
                        inputPuan.setHint("Puan (1-5)");
                        inputPuan.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                        layout.addView(inputPuan);
                        builder.setView(layout);
                        builder.setPositiveButton("Kaydet", (dialog, whichBtn) -> {
                              String ad = inputAd.getText().toString().trim();
                              String yorum = inputYorum.getText().toString().trim();
                              String puanStr = inputPuan.getText().toString().trim();
                              int puan = 0;
                              try { puan = Integer.parseInt(puanStr); } catch (Exception e) {}
                              if (ad.isEmpty() || yorum.isEmpty() || puan < 1 || puan > 5) {
                                    Toast.makeText(context, "Tüm alanları doldurun ve puan 1-5 arası olmalı!", Toast.LENGTH_SHORT).show();
                                    return;
                              }
                              Review review = new Review(0, kitap.getId(), ad, yorum, puan);
                              bookManager.yorumEkle(review);
                              Toast.makeText(context, "Yorum kaydedildi!", Toast.LENGTH_SHORT).show();
                        });
                        builder.setNegativeButton("İptal", null);
                        builder.show();
                  });

                  return convertView;
            }

            private class ViewHolder {
                  TextView kitapAd;
                  TextView yazar;
                  TextView yil;
                  TextView aciklama;
                  TextView kategori;
                  ImageView favori;
                  // Button btnOduncVer; // Kaldırıldı
                  Button btnYorumEkle;
                  TextView textViewOduncDurumu; // Eklendi
            }
      }
}