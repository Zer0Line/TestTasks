# TestTasks

**Использование библиотеки emailtextview-release.aar**
1. Добавить в манифест
uses-permission android:name="android.permission.INTERNET"
2. Добавить в  gradle проекта
compile 'com.github.bumptech.glide:glide:4.0.0'

**Пример использования**
```xml
<ru.gurucode.emailtextview.EmailTextView
    android:id="@+id/emailtv"
    android:inputType="textMultiLine"
    android:text="привет это oblomov@mail.ru "
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:textSize="25sp"/>
```
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    EmailTextView etv = (EmailTextView)findViewById(R.id.emailtv);
    //Назначить callback для клика по чипу
    etv.onChipClickListener = new OnChipClickListener() {
        @Override
        public void onChipClick(String email) {
            Toast.makeText(MainActivity.this, email, Toast.LENGTH_SHORT)
                    .show();
        }
    };

    etv.setText("тестовое задание oblomov@mail.ru");
    etv.mFoolEmailInChip = true;
}
```
