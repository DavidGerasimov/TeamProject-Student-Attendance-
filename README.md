# TeamProject-Student-Attendance-
Student Project about developing a Student Attendance application with a supabase database using android Studio and Java with APIs Group Project
# Систем за Евиденција на Студенти / Student Registration System
Развој на мобилни апликации — Тимска проектна задача

## Автори
- David Gerasimov — 102747
- Kristina Petrova — 102749
- Dijana Parceklieva — 102707

## За проектот
Систем за евиденција на присуство на студенти користејќи NFC технологија. Се состои од две Android апликации (Teacher App и Student App) и веб dashboard.

## Веб Dashboard
https://davidgerasimov.github.io/TeamProject-Student-Attendance-

## Компоненти
Teacher App — најава со УГД е-маил, NFC скенирање на студенти, избор и додавање предмети, евиденција на присуство, детекција на дупликати.

Student App — најава со студентски УГД е-маил, HCE NFC емитување на шифриран токен, регистрација на нови студенти.

Web Dashboard — приказ на евиденција со филтри, Chart.js графикони, bulk sync, пагинација, role-based access (teacher/admin).

## NFC Безбедност
Student App емитува SHA-256 шифриран токен во форматот: studentId|timestamp|hash. Токенот истекува секој час. Teacher App валидира токенот и блокира дупликат скенирања.

## Технологии
Android Studio + Java, Supabase (PostgreSQL + REST API), OkHttp3, Chart.js, GitHub Pages, SHA-256, HCE.

## Напомена за backend
Наместо PHP/MySQL се користи Supabase — moderna hosted PostgreSQL база со REST API која обезбедува идентична функционалност. Употребата на AI е дозволена според упатствата на предметот.

## Референци
- https://supabase.com/docs
- https://square.github.io/okhttp/
- https://www.chartjs.org
- https://developer.android.com/guide/topics/connectivity/nfc/hce
