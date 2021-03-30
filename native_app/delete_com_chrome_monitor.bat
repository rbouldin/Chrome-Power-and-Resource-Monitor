:: Copyright 2014 The Chromium Authors. All rights reserved.
:: Use of this source code is governed by a BSD-style license that can be
:: found in the LICENSE file.

:: Deletes the entry created by add_com_chrome_monitor.bat
REG DELETE "HKCU\Software\Google\Chrome\NativeMessagingHosts\com.chrome.monitor" /f
REG DELETE "HKLM\Software\Google\Chrome\NativeMessagingHosts\com.chrome.monitor" /f