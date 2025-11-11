package com.example.appdoctruyentranh

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val loginManager = LoginManager.getInstance()
    val callbackManager = remember { CallbackManager.Factory.create() }

    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                Toast.makeText(context, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show()
                                navController.navigate("home")
                            } else {
                                val exception = authTask.exception
                                if (exception is FirebaseAuthUserCollisionException) {
                                    Toast.makeText(context, "Email đã được sử dụng với một phương thức đăng nhập khác.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Firebase Auth thất bại: ${exception?.message}", Toast.LENGTH_LONG).show()
                                }
                                Log.e("GoogleSignIn", "Firebase Auth Failed", exception)
                            }
                        }
                } catch (e: ApiException) {
                    Toast.makeText(context, "Lỗi Google Sign-In: ${e.statusCode}", Toast.LENGTH_LONG).show()
                    Log.e("GoogleSignIn", "Google Sign-In Failed", e)
                }
            } else {
                Toast.makeText(context, "Đăng nhập Google đã bị hủy.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val facebookLoginLauncher = rememberLauncherForActivityResult(
        contract = loginManager.createLogInActivityResultContract(callbackManager, null),
        onResult = { /* The result is handled in the callback */ }
    )

    DisposableEffect(Unit) {
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Đăng nhập Facebook thành công!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home")
                        } else {
                            val exception = task.exception
                             if (exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(context, "Email đã được sử dụng với một phương thức đăng nhập khác.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Xác thực Firebase thất bại: ${exception?.message}", Toast.LENGTH_LONG).show()
                            }
                            Log.e("FacebookSignIn", "Firebase Auth with Facebook Failed", exception)
                        }
                    }
            }

            override fun onCancel() {
                Toast.makeText(context, "Đăng nhập Facebook đã bị hủy.", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(context, "Lỗi Facebook SDK: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("FacebookSignIn", "Facebook SDK Error", error)
            }
        })

        onDispose {
            loginManager.unregisterCallback(callbackManager)
        }
    }


    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE1E0FF), Color(0xFFF8F8FF))
    )
    val facebookButtonColor = Color(0xFF1877F2)
    val loginButtonColor = Color(0xFFB0B0B0)
    val lightTextColor = Color.Gray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Đăng Nhập",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Sign in to start",
                fontSize = 16.sp,
                color = lightTextColor
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Đăng nhập bằng Google",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { facebookLoginLauncher.launch(listOf("email", "public_profile")) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = facebookButtonColor,
                    contentColor = Color.White
                )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_facebook),
                    contentDescription = "Facebook Icon",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Đăng nhập bằng Facebook",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            ClickableText(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = lightTextColor, fontSize = 14.sp)) {
                        append("Haven't account? ")
                    }
                    withStyle(style = SpanStyle(
                        color = facebookButtonColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )) {
                        append("Sign up!")
                    }
                },
                onClick = { offset ->
                    if (offset > 17) {
                        navController.navigate("register")
                    }
                }
            )

            Spacer(modifier = Modifier.height(300.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(gradientBrush)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Nhập email hoặc số điện thoại",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomPasswordTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Mật khẩu"
            )

            TextButton(
                onClick = { navController.navigate("forgot_password") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Quên mật khẩu",
                    color = lightTextColor,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home")
                                } else {
                                    Toast.makeText(context, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = loginButtonColor,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Đăng Nhập", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
